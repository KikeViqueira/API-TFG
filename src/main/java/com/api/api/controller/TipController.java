package com.api.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.api.DTO.TipDetailDTO;
import com.api.api.DTO.TipDTO.*;
import com.api.api.service.TipService;


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
    public ResponseEntity<List<TipResponseDTO>> getTips(@PathVariable("idUser") Long idUser){
        //llamamos a la función que se encarga de recuperar los tips de la BD
        List<TipResponseDTO> tips = this.tipService.getTips(idUser);
        return ResponseEntity.ok(tips);
    }
    
    //Endpoint para crear un tip en la BD, 
    @PostMapping("/{idUser}/tips")
    public ResponseEntity<TipGeneratedDTO> createTip(@PathVariable("idUser") Long idUser){
        //llamamos a la función que se encarga de crear un tip y guardarlo en la BD
        TipGeneratedDTO tipCreated = this.tipService.createTip(idUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipCreated);
    }

    //Endpoint para eliminar un tip o varios de la sección de tips de la app
    @DeleteMapping("/{idUser}/tips/{id}")
    public ResponseEntity<List<TipResponseDTO>> deleteTip(@PathVariable("idUser") Long idUser, @RequestBody List<Long> ids){
        //llamamos a la función que se encarga de eliminar el tip de la BD
        List<TipResponseDTO> deletedTips = this.tipService.deleteTip(idUser,ids);
        return ResponseEntity.ok(deletedTips);
    }

    //Endpoint para recuperar la info detallada del tip en el que el user pincha en la app
    @GetMapping("/{idUser}/tips/{id}")
    public ResponseEntity<TipDetailDTO> getDetailTip(@PathVariable("idUser") Long id){
        //llamamos a la función del service y en base a los que nos devuelva devolvemos un status u otro
        return ResponseEntity.ok(this.tipService.getDetailsTip(id));
    }

    /*
     * Endpoints relacionados con los tips de un user y sus favoritos 
     */
    //Endpoint para recuperar los tips favoritos de un user
    @GetMapping("/{idUser}/favorites-tips")
    public ResponseEntity<List<TipFavDTO>> getFavoritesTips(@PathVariable("idUser") Long idUser){
        List<TipFavDTO> favoriteTips = this.tipService.getFavoritesTips(idUser);
        return ResponseEntity.ok(favoriteTips);
    }

    //Endpoint para eliminar un tip de los favoritos de un user
    @DeleteMapping("/{idUser}/favorites-tips/{idUserTip}")
    public ResponseEntity<TipFavDTO> deleteFavoriteTip(@PathVariable("idUser") Long id, @PathVariable("idUserTip") Long idTip){
        //llamamos a la función del service que se encarga de esta lógica
        TipFavDTO tipDTO = this.tipService.deleteFavoriteTip(id, idTip);
        return ResponseEntity.ok(tipDTO);
    }

    //Endpoint para añadir un tip a los favoritos de un user
    @PostMapping("/{id}/favorites-tips/{idTip}")
    public ResponseEntity<TipFavDTO> addFavoriteTip(@PathVariable("id") Long id, @PathVariable("idTip") Long idTip){
        //Llamamos a la función del service que se encarga de esta lógica
        TipFavDTO tipDTO = this.tipService.addFavoriteTip(id, idTip);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipDTO); //Guardado correctamente en la lista de favoritos del user
    }
}
