package com.api.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.api.DTO.CloudinaryUploadDTO;
import com.api.api.DTO.SoundDTO.DeleteSoundDTO;
import com.api.api.DTO.SoundDTO.ResponseSoundDTO;
import com.api.api.exceptions.DuplicatedSoundName;
import com.api.api.exceptions.NoContentException;
import com.api.api.model.Sound;
import com.api.api.model.User;
import com.api.api.repository.SoundRepository;
import com.api.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SoundService {

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public SoundService(SoundRepository soundRepository, UserRepository userRepository){
        this.soundRepository = soundRepository;
        this.userRepository = userRepository;
    }

    //Función para recuperra todos los sonidos estáticos de la BD
    public List<ResponseSoundDTO> getAllStaticSounds(){
        List<ResponseSoundDTO> staticSounds = new ArrayList<>();
        List<Sound> sounds = this.soundRepository.findByIsDefaultTrue();
        if (!sounds.isEmpty()){
            //Pasamos los sonidos recuperados al formato que queremos devolver
            for (Sound sound : sounds) staticSounds.add(new ResponseSoundDTO(sound));
            return staticSounds;
        } 
        else throw new NoContentException("No hay sonidos estáticos en la BD");
    }


    //Recuperamos los sonidos que el user ha subido a la app
    public List<ResponseSoundDTO> getUserSounds(Long idUser){
        List<ResponseSoundDTO> userSounds = new ArrayList<>();
        //llamamos la bd para saber los sonidos que tienen de dueño al user con la id recibida
        List<Sound> sounds = this.soundRepository.findByOwnerId(idUser);
        if (!sounds.isEmpty()){
            //Pasamos los sonidos recuperados al formato que queremos devolver
            for (Sound sound : sounds) userSounds.add(new ResponseSoundDTO(sound));
            return userSounds;
        }
        else throw new NoContentException("No hay sonidos subidos por este usuario");
    }

    //Función para que el user pueda crear un sonido en la app
    public ResponseSoundDTO createSound(Long id, MultipartFile file){
        //Comprobamos si el user existe
        User user = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        
        // Extraer el nombre original del archivo
        String originalFileName = file.getOriginalFilename();
        String nameWithoutExtension = originalFileName != null ? 
            originalFileName.replaceFirst("[.][^.]+$", "") : 
            "sound_" + System.currentTimeMillis();
        
        // Verificar si el usuario ya tiene un sonido con el mismo nombre
        if (this.soundRepository.existsByOwnerIdAndName(id, nameWithoutExtension)) throw new DuplicatedSoundName("Ya tienes un sonido con este nombre. Por favor, usa un nombre diferente.");
        
        //Tenemos que llamar a la función de cloudinary para subir el sonido en la nube y recuperar los atributos que guardaremos en la bd
        CloudinaryUploadDTO cloudinaryUploadDTO = this.cloudinaryService.uploadMultipartFile(file, true);
        
        Sound sound = new Sound();
        sound.setName(cloudinaryUploadDTO.getResourceName());
        sound.setSource(cloudinaryUploadDTO.getUrl());
        sound.setPublicIdCloudinary(cloudinaryUploadDTO.getPublicId());
        sound.setDefault(false);
        sound.setOwner(user);
        this.soundRepository.save(sound);
        return new ResponseSoundDTO(sound);
    }

    //Función para eliminar un sonido de los que ha subido el user a la app
    public DeleteSoundDTO deleteSoundUser(Long idUser, Long idSound){
        this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Tenemos que comprobar que el sonido exista en la bd
        Sound sound = this.soundRepository.findById(idSound).orElseThrow(() -> new EntityNotFoundException("El sonido no existe"));
        //Tenemos que mirar si el sonido que está intentando eliminar el user es estático, estos solo los puede eliminar un admin
        if (sound.isDefault()) throw new AccessDeniedException("No se puede eliminar un sonido estático");
        //Tenemos que comprobar que exista la relación entre ambas entidades
        boolean exits = this.soundRepository.existsByOwnerIdAndId(idUser, idSound); 
        //la realación existe por lo que podemos eliminar el sonido de la bd y hibernate ya desvinculara el sonido del user
        if (exits){
            //llamamos a la función de cloudinary para eliminar el sonido de la nube
            if (Objects.nonNull(sound.getPublicIdCloudinary())) this.cloudinaryService.deleteFile(sound.getPublicIdCloudinary(), true);
            this.soundRepository.delete(sound);
            return new DeleteSoundDTO(sound);
        } else throw new EntityNotFoundException("La relación entre el usuario y el sonido no existe");
    }
}
