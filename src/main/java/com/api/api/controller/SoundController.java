package com.api.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.api.api.DTO.SoundDTO.DeleteSoundDTO;
import com.api.api.DTO.SoundDTO.ResponseSoundDTO;
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
    public ResponseEntity<List<ResponseSoundDTO>> getAllStaticsSounds(){
        //llamamos a la función del servicio que se encarga de devolvernos la lista de ellos
        List<ResponseSoundDTO> staticSounds = this.soundService.getAllStaticSounds();
        return ResponseEntity.ok(staticSounds);
    }

    //Endpoint para obtener los sonidos de los users 
    @PreAuthorize("hasPermission(#idUser, 'owner')")
    @GetMapping("/{idUser}")
    public ResponseEntity<List<ResponseSoundDTO>> getUserSounds(@PathVariable("idUser") Long idUser ){
        List<ResponseSoundDTO> userSounds = this.soundService.getUserSounds(idUser);
        return ResponseEntity.ok(userSounds);
    }

    //Endpoint para que el user pueda crear un sonido
    @PreAuthorize("hasPermission(#idUser, 'owner')")
    @PostMapping(path = "/{idUser}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseSoundDTO> createSound(@PathVariable("idUser") Long idUser, @RequestParam("file") MultipartFile file){
        //llamamos a la función encargada de crear el sonido
        ResponseSoundDTO createdSound = this.soundService.createSound(idUser, file);
        return ResponseEntity.ok(createdSound);
    }

    @PreAuthorize("hasPermission(#idUser, 'owner')")
    @DeleteMapping("/{idUser}/{idSound}")
    public ResponseEntity<DeleteSoundDTO> deleteSoundUser(@PathVariable("idUser") Long idUser, @PathVariable("idSound") Long idSound){
        //llamamos a la función que se encarga de esta lógica
        DeleteSoundDTO soundDTO = this.soundService.deleteSoundUser(idUser, idSound);
        return ResponseEntity.ok(soundDTO);
    }
}
