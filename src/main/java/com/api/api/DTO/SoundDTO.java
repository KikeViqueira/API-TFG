package com.api.api.DTO;

import com.api.api.model.Sound;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoundDTO {

    //DTO para devolver solo los campos necesarios al user que llama al endpoint de la api, para recuperar sonidos
    private Long id;
    private String name;
    private String fileUrl;

    public SoundDTO(Sound sound){
        this.id = sound.getId();
        this.name = sound.getName();
        this.fileUrl = sound.getFileUrl();
    }
}
