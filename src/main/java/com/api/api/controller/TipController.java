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

import com.api.api.DTO.UserDTO;
import com.api.api.model.Tip;
import com.api.api.model.TipDetail;
import com.api.api.model.User;
import com.api.api.service.PatchUtils;
import com.api.api.service.TipService;
import com.api.api.service.UserService;
import com.github.fge.jsonpatch.JsonPatchException;

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
    public ResponseEntity<List<Tip>> getTips(){
        //Devolvemos la lista de tips en caso de que existan, si no devolvemos Not Found
        List<Tip> tips = tipService.getTips();
        if (tips.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(tips);
    }
    
    //Endpoint para crear un tip en la BD
    @PostMapping
    public ResponseEntity<?> createTip(@RequestBody @Valid Tip tip){
        //Tenemos que comprobar que el tip no existe ya en la BD, si no devolveremos un error de conflicto al intentar crear un recurso ya existente
        Tip tipRecuperado = tipService.geTip(tip.getId());
        if (tipRecuperado != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El tip que se está intentando crear ya existe.");
        }else{
        //llamamos a la función que se encarga de guardar el tip en la BD, las comprobaciones de los campos ya los hace la anotación @Valid
        Tip tipCreado = tipService.createTip(tip);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipCreado);
        }
    }

    //Endpoint para eliminar un tip de la sección de tips de la app
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTip(@PathVariable Long id){
        //Comprobamos que el tip que se está intentando eliminar está en la BD
        Tip tipRecuperado = tipService.geTip(id);
        if (tipRecuperado == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El tip que se está intentando eliminar no existe.");
        //En caso de que exista llamamos a la función para eliminarlo de la BD
        return ResponseEntity.noContent().build();
    }


    //Endpoint para recuperar la info detallada del tip
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetailTip(@PathVariable Long id){
        //llamamos a la función del service y en base a los que nos devuelva devolvemos un status u otro
        TipDetail tipDetail = tipService.getDetailsTip(id);
        if (tipDetail == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El tip que se está intentando recuperar no tiene detalles.");
        return ResponseEntity.ok(tipDetail);
    }
}
