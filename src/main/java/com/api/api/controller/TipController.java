package com.api.api.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.api.api.DTO.TipDTO;
import com.api.api.DTO.UserDTO;
import com.api.api.model.Tip;
import com.api.api.model.TipDetail;
import com.api.api.model.User;
import com.api.api.service.PatchUtils;
import com.api.api.service.TipService;
import com.api.api.service.UserService;
import com.github.fge.jsonpatch.JsonPatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.val;

@RestController
@RequestMapping("/api/tips")
public class TipController {

    //Creamos una instancia privada del servicio correspondiente para poder invocar a sus funciones
    private TipService tipService;

    @Autowired //Inyectamos las dependencias necesarias
    public TipController(TipService tipService){
        this.tipService = tipService;
    }

    //Endpoint para recuperar la lista de tips para la página tips de la app
    @GetMapping
    public ResponseEntity<List<TipDTO.TipResponseDTO>> getTips(){
        try {
            //llamamos a la función que se encarga de recuperar los tips de la BD
            List<TipDTO.TipResponseDTO> tips = tipService.getTips();
            return ResponseEntity.ok(tips);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }
    
    //Endpoint para crear un tip en la BD, //TODO: REHACER ESTE ENDPOINT PARA QUE LA IA DEVUELVA EL CUERPO AL SERVICE Y ESTE LO GUARDE EN LA BD
    @PostMapping
    public ResponseEntity<?> createTip(@RequestBody @Valid Tip tip){
        try {
            //llamamos a la función que se encarga de crear un tip y guardarlo en la BD
            TipDTO.TipResponseDTO tipCreado = tipService.createTip(tip);
            return ResponseEntity.status(HttpStatus.CREATED).body(tipCreado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El tip que se está intentando crear ya existe.");
        }
    }

    //Endpoint para eliminar un tip de la sección de tips de la app
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTip(@PathVariable Long id){
        try {
            //llamamos a la función que se encarga de eliminar el tip de la BD
            tipService.deleteTip(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El tip que se está intentando eliminar no existe.");
        }
    }

    //Endpoint para recuperar la info detallada del tip en el que el user pincha en la app
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetailTip(@PathVariable Long id){
        try {
            //llamamos a la función del service y en base a los que nos devuelva devolvemos un status u otro
            return ResponseEntity.ok(tipService.getDetailsTip(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El tip que se está intentando recuperar no tiene detalles.");
        }
    }
}
