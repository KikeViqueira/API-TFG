package com.api.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.api.api.DTO.SoundDTO;
import com.api.api.model.Sound;
import com.api.api.repository.SoundRepository;

@Service
public class SoundService {

    private SoundRepository soundRepository;

    public SoundService(SoundRepository soundRepository){
        this.soundRepository = soundRepository;
    }

    //Función para recuperra todos los sonidos estáticos de la BD
    public List<SoundDTO> getAllStaticSounds(){
        List<SoundDTO> soundsDto = new ArrayList<>();
        List<Sound> sounds = soundRepository.findByIsDefaultTrue();
        //Hacemos la conversión de el objeto recuperado a su correspondiente DTO para devolver solo los campos necesarios
        for (Sound sound : sounds) soundsDto.add(new SoundDTO(sound));
        return soundsDto;
    }
    
}
