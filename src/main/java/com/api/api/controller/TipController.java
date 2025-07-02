package com.api.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.api.api.DTO.TipDetailDTO;
import com.api.api.DTO.TipDTO.*;
import com.api.api.service.TipService;


@RestController
@PreAuthorize("hasPermission(#idUser, 'owner')")
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

    /**
     * Endpoint para recuperar los tips que el user ha generado y guardado en la BD con paginación
     * 
     * @param idUser ID del usuario
     * @param page Número de página (empieza en 0). Por defecto: 0
     * @param size Número de elementos por página. Por defecto: 10
     * @param sortBy Campo por el que ordenar (timeStamp, title, etc.). Por defecto: timeStamp
     * @param sortDirection Dirección del ordenamiento (asc/desc). Por defecto: desc
     * @return Page<TipResponseDTO> con la información paginada
     * 
     * Ejemplo de uso:
     * GET /api/users/1/tips?page=0&size=5&sort=timeStamp&direction=desc
     * GET /api/users/1/tips (usa valores por defecto)
     */
    @GetMapping("/{idUser}/tips")
    public ResponseEntity<Page<TipResponseDTO>> getTips(
            @PathVariable("idUser") Long idUser,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "7") int size,
            @RequestParam(value = "sort", defaultValue = "timeStamp") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String sortDirection) {
        
        // Validar que page y size sean valores válidos
        if (page < 0) page = 0;
        if (size <= 0) size = 7;
        if (size > 10) size = 10; // Limitar el tamaño máximo de página
        
        // Crear el objeto Sort basado en los parámetros
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        
        // Crear el Pageable con la ordenación
        Pageable pageable = PageRequest.of(page, size, sort);
        
        //llamamos a la función que se encarga de recuperar los tips de la BD
        Page<TipResponseDTO> tips = this.tipService.getTips(idUser, pageable);
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
    @DeleteMapping("/{idUser}/tips")
    public ResponseEntity<List<TipResponseDTO>> deleteTip(@PathVariable("idUser") Long idUser, @RequestBody List<Long> ids){
        //llamamos a la función que se encarga de eliminar el tip de la BD
        List<TipResponseDTO> deletedTips = this.tipService.deleteTip(idUser,ids);
        return ResponseEntity.ok(deletedTips);
    }

    //Endpoint para recuperar la info detallada del tip en el que el user pincha en la app
    @GetMapping("/{idUser}/tips/{id}")
    public ResponseEntity<TipDetailDTO> getDetailTip(@PathVariable("idUser") Long idUser, @PathVariable("id") Long idTip){
        //llamamos a la función del service y en base a los que nos devuelva devolvemos un status u otro
        return ResponseEntity.ok(this.tipService.getDetailsTip(idUser, idTip));
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
    @DeleteMapping("/{idUser}/favorites-tips/{idTip}")
    public ResponseEntity<TipFavDTO> deleteFavoriteTip(@PathVariable("idUser") Long idUser, @PathVariable("idTip") Long idTip){
        //llamamos a la función del service que se encarga de esta lógica
        TipFavDTO tipDTO = this.tipService.deleteFavoriteTip(idUser, idTip);
        return ResponseEntity.ok(tipDTO);
    }

    //Endpoint para añadir un tip a los favoritos de un user
    @PostMapping("/{idUser}/favorites-tips/{idTip}")
    public ResponseEntity<TipFavDTO> addFavoriteTip(@PathVariable("idUser") Long idUser, @PathVariable("idTip") Long idTip){
        //Llamamos a la función del service que se encarga de esta lógica
        TipFavDTO tipDTO = this.tipService.addFavoriteTip(idUser, idTip);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipDTO); //Guardado correctamente en la lista de favoritos del user
    }
}
