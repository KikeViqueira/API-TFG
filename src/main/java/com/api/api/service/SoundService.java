package com.api.api.service;

import java.util.ArrayList;
import java.util.List;

import javax.management.relation.RelationNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.MethodNotAllowed;
import org.springframework.web.server.MethodNotAllowedException;

import com.api.api.DTO.SoundDTO;
import com.api.api.DTO.SoundDTO.DeleteSoundDTO;
import com.api.api.DTO.SoundDTO.ResponseSoundDTO;
import com.api.api.exceptions.NoContentException;
import com.api.api.exceptions.RelationshipAlreadyExistsException;
import com.api.api.model.Sound;
import com.api.api.model.User;
import com.api.api.repository.SoundRepository;
import com.api.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SoundService {

    private SoundRepository soundRepository;

    private UserRepository userRepository;

    public SoundService(SoundRepository soundRepository, UserRepository userRepository){
        this.soundRepository = soundRepository;
        this.userRepository = userRepository;
    }

    //Función para recuperra todos los sonidos estáticos de la BD
    public List<ResponseSoundDTO> getAllStaticSounds(){
        List<ResponseSoundDTO> staticSounds = new ArrayList<>();
        List<Sound> sounds = soundRepository.findByIsDefaultTrue();
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
        List<Sound> sounds = soundRepository.findByOwnerId(idUser);
        if (!sounds.isEmpty()){
            //Pasamos los sonidos recuperados al formato que queremos devolver
            for (Sound sound : sounds) userSounds.add(new ResponseSoundDTO(sound));
            return userSounds;
        }
        else throw new NoContentException("No hay sonidos subidos por el usuario con id: " + idUser);
    }

    //Función para que el user pueda crear un sonido en la app
    public ResponseSoundDTO createSound(Long id, Sound sound){
        //Comprobamos si el user existe
        User user = userRepository.findById(id).orElse(null);
        if (user != null){
            //Comprobamos que el sonido que el user quiere crear ya no lo haya subido anteriormente
            boolean exists = soundRepository.existsByOwnerIdAndFileUrl(id, sound.getFileUrl());
            if (!exists){
                //Si el sonido no existe en la BD entonces lo creamos
                sound.setDefault(false);
                sound.setOwner(user);
                soundRepository.save(sound);
                return new ResponseSoundDTO(sound);
            } else throw new RelationshipAlreadyExistsException("El sonido ya ha sido subido por el usuario");
        }else throw new EntityNotFoundException("El usuario con id: " + id + " no existe");
    }

    //Función para eliminar un sonido de los que ha subido el user a la app
    public DeleteSoundDTO deleteSoundUser(Long idUser, Long idSound){
        //Tenemos que comprobar que el sonido exista en la bd
        Sound sound = soundRepository.findById(idSound).orElse(null);
        if (sound != null){
            //Tenemos que mirar si el sonido que está intentando eliminar el user es estático, estos solo los puede eliminar un admin
            if (sound.isDefault()) throw new AccessDeniedException("No se puede eliminar un sonido estático");
            //Tenemos que comprobar que exista la relación entre ambas entidades
            boolean exits = soundRepository.existsByOwnerIdAndFileUrl(idUser, sound.getFileUrl()); 
            //la realación existe por lo que podemos eliminar el sonido de la bd y hibernate ya desvinculara el sonido del user
            if (exits){
                soundRepository.delete(sound);
                return new DeleteSoundDTO(sound);
            } else throw new EntityNotFoundException("La relación entre el usuario y el sonido no existe");
        } else throw new EntityNotFoundException("El sonido con id: " + idSound + " no existe");
    }
}
