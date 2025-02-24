package com.api.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.api.api.DTO.SoundDTO;
import com.api.api.model.Sound;
import com.api.api.model.User;
import com.api.api.repository.SoundRepository;
import com.api.api.repository.UserRepository;

@Service
public class SoundService {

    private SoundRepository soundRepository;

    private UserRepository userRepository;

    public SoundService(SoundRepository soundRepository, UserRepository userRepository){
        this.soundRepository = soundRepository;
        this.userRepository = userRepository;
    }

    //Función para recuperra todos los sonidos estáticos de la BD
    public List<SoundDTO> getAllStaticSounds(){
        List<SoundDTO> soundsDto = new ArrayList<>();
        List<Sound> sounds = soundRepository.findByIsDefaultTrue();
        //Hacemos la conversión de el objeto recuperado a su correspondiente DTO para devolver solo los campos necesarios
        for (Sound sound : sounds) soundsDto.add(new SoundDTO(sound));
        return soundsDto;
    }


    //Recuperamos los sonidos que el user ha subido a la app
    public List<SoundDTO> getUserSounds(Long idUser){
        List<SoundDTO> soundsDTOs = new ArrayList<>();
        //llamamos la bd para saber los sonidos que tienen de dueño al user con la id recibida
        List<Sound> sounds = soundRepository.findByOwnerId(idUser);
        //Si el user tiene sonidos, los pasamos a DTO con la info necesario
        if (!sounds.isEmpty()){
            for (Sound sound : sounds) soundsDTOs.add(new SoundDTO(sound));
            return soundsDTOs;
        }
        return soundsDTOs;
    }

    //Función para que el user pueda crear un sonido en la app
    public SoundDTO createSound(Long id, Sound sound){
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
                return new SoundDTO(sound);
            }
            //TODO: LANZAR EXCEPCION RELACION YA EXISTENTE
        }
        //TODO: LANZAR EXCEPCION DE ENTITIY NO ENCONTRADA
        return null;
    }

    //Función para eliminar un sonido de los que ha subido el user a la app
    public SoundDTO deleteSoundUser(Long idUser, Long idSound){
        //Tenemos que comprobar que el sonido exista en la bd
        Sound sound = soundRepository.findById(idSound).orElse(null);
        if (sound != null){
            //Tenemos que comprobar que exista la relación entre ambas entidades
            boolean exits = soundRepository.existsByOwnerIdAndFileUrl(idUser, sound.getFileUrl()); 
            //la realación existe por lo que podemos eliminar el sonido de la bd y hibernate ya desvinculara el sonido del user
            if (exits){
                soundRepository.delete(sound);
                return new SoundDTO(sound);
            }
        }
        return null;
    }
}
