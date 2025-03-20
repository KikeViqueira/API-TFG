package com.api.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.SoundDTO;
import com.api.api.DTO.SoundDTO.DeleteSoundDTO;
import com.api.api.DTO.SoundDTO.ResponseSoundDTO;
import com.api.api.model.Sound;
import com.api.api.service.SoundService;

import jakarta.validation.Valid;

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
    public ResponseEntity<List<ResponseSoundDTO>> getAllStaticsSounds(){
        //llamamos a la función del servicio que se encarga de devolvernos la lista de ellos
        List<ResponseSoundDTO> staticSounds = soundService.getAllStaticSounds();
        return ResponseEntity.ok(staticSounds);
    }


    //TODO: ENDPOINT PARA OBTENER LOS SONIDOS QUE EL USER HA SUBIDO A LA APP, SOLO PUEDEN LLAMARLO LOS DUEÑOS DEL AUDIO
    //Endpoint para obtener los sonidos de los users 
    @GetMapping("/{idUser}")
    public ResponseEntity<List<ResponseSoundDTO>> getUserSounds(@PathVariable("idUser") Long idUser ){
        List<ResponseSoundDTO> userSounds = soundService.getUserSounds(idUser);
        return ResponseEntity.ok(userSounds);
    }

    //Endpoint para que el user pueda crear un sonido
    @PostMapping("/{idUser}")
    public ResponseEntity<ResponseSoundDTO> createSound(@PathVariable("idUser") Long idUser, @RequestBody @Valid Sound sound){
        //llamamos a la función encargada de crear el sonido
        ResponseSoundDTO createdSound = soundService.createSound(idUser, sound);
        return ResponseEntity.ok(createdSound);
    }

    //TODO: HACER EL ENDPOINT DE ELIMINAR UN SONIDO SI EL USUARIO YA NO LO QUIERE
    @DeleteMapping("/{idUser}/{idSound}")
    public ResponseEntity<DeleteSoundDTO> deleteSoundUser(@PathVariable("idUser") Long idUser, @PathVariable("idSound") Long idSound){
        //llamamos a la función que se encarga de esta lógica
        DeleteSoundDTO soundDTO = soundService.deleteSoundUser(idUser, idSound);
        return ResponseEntity.ok(soundDTO);
    }
}
