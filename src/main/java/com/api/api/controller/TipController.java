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
@RequestMapping("/api/users")
public class TipController {

    /*
     * Controller que tiene toda la lógica de los tips de un en la app, tanto de los tips que ha generado en base al uso de la propia app
     * como de los tips que ha guardado en su lista de favoritos para tenerlos a mano de una manera más rápida
     */

    //Creamos una instancia privada del servicio correspondiente para poder invocar a sus funciones
    @Autowired
    private TipService tipService;

    
    /*
     * Endpoints relacionados con las pestaña de tips de la app y del user y su correspondiente gestión 
     */

    //Endpoint para recuperar los tips que el user ha generado y guardado en la BD
    @GetMapping("/{idUser}/tips")
    public ResponseEntity<List<TipDTO.TipResponseDTO>> getTips(){
        //llamamos a la función que se encarga de recuperar los tips de la BD
        List<TipDTO.TipResponseDTO> tips = tipService.getTips();
        return ResponseEntity.ok(tips);
    }
    
    //Endpoint para crear un tip en la BD, 
    //TODO: REHACER ESTE ENDPOINT PARA QUE LA IA DEVUELVA EL CUERPO AL SERVICE Y ESTE LO GUARDE EN LA BD
    @PostMapping
    public ResponseEntity<?> createTip(@RequestBody @Valid Tip tip){
        //llamamos a la función que se encarga de crear un tip y guardarlo en la BD
        TipDTO.TipResponseDTO tipCreado = tipService.createTip(tip);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipCreado);
    }

    //Endpoint para eliminar un tip de la sección de tips de la app
    @DeleteMapping("/{idUser}/tips/{id}")
    public ResponseEntity<?> deleteTip(@PathVariable Long id){
        //llamamos a la función que se encarga de eliminar el tip de la BD
        TipDTO.TipResponseDTO tipEliminado = tipService.deleteTip(id);
        return ResponseEntity.ok(tipEliminado);
    }

    //Endpoint para recuperar la info detallada del tip en el que el user pincha en la app
    @GetMapping("/{idUser}/tips/{id}")
    public ResponseEntity<?> getDetailTip(@PathVariable Long id){
        //llamamos a la función del service y en base a los que nos devuelva devolvemos un status u otro
        return ResponseEntity.ok(tipService.getDetailsTip(id));
    }

    /*
     * Endpoints relacionados con los tips de un user y sus favoritos 
     */
    //Endpoint para recuperar los tips favoritos de un user
    @GetMapping("/{idUser}/favorites-tips")
    public ResponseEntity<List<TipDTO.TipFavDTO>> getFavoritesTips(@PathVariable("idUser") Long idUser){
        List<TipDTO.TipFavDTO> favoriteTips = tipService.getFavoritesTips(idUser);
        return ResponseEntity.ok(favoriteTips);
    }

    //Endpoint para eliminar un tip de los favoritos de un user
    @DeleteMapping("/{idUser}/favorites-tips/{idUserTip}")
    public ResponseEntity<TipDTO.TipFavDTO> deleteFavoriteTip(@PathVariable("id") Long id, @PathVariable("idTip") Long idTip){
        //llamamos a la función del service que se encarga de esta lógica
        TipDTO.TipFavDTO tipDTO = tipService.deleteFavoriteTip(id, idTip);
        return ResponseEntity.ok(tipDTO);
    }

    //Endpoint para añadir un tip a los favoritos de un user
    @PostMapping("/{id}/favorites-tips/{idTip}")
    public ResponseEntity<TipDTO.TipFavDTO> addFavoriteTip(@PathVariable("id") Long id, @PathVariable("idTip") Long idTip){
        //Llamamos a la función del service que se encarga de esta lógica
        TipDTO.TipFavDTO tipDTO = tipService.addFavoriteTip(id, idTip);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipDTO); //Guardado correctamente en la lista de favoritos del user
    }
}
