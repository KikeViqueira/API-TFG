package com.api.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.SoundDTO;
import com.api.api.model.Sound;
import com.api.api.service.SoundService;

@RestController
@RequestMapping("/api/sounds")
public class SoundController {

    //Definimos una variable del servicio correspondiente
    private SoundService soundService;

    @Autowired
    public SoundController(SoundService soundService){
        this.soundService = soundService;
    }

    //Endpoint para obtener todos los sonidos estáticos
    @GetMapping
    public ResponseEntity<List<SoundDTO>> getAllStaticsSounds(){
        //llamamos a la función del servicio que se encarga de devolvernos la lista de ellos
        List<SoundDTO> staticSounds = soundService.getAllStaticSounds();
        if (staticSounds.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(staticSounds);
    }

    

    
    
}
