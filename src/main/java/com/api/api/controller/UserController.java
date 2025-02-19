package com.api.api.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.api.api.DTO.SoundDTO;
import com.api.api.DTO.TipDTO;
import com.api.api.DTO.UserDTO;
import com.api.api.model.Sound;
import com.api.api.model.Tip;
import com.api.api.model.User;
import com.api.api.service.PatchUtils;
import com.api.api.service.UserService;
import com.github.fge.jsonpatch.JsonPatchException;

import jakarta.validation.Valid;
import lombok.val;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;
    private PatchUtils patchUtils;

    //Definimos el constructor de la clase
    @Autowired //Inyección de dependencias en el constructor de la clase
    public UserController(UserService userService, PatchUtils patchUtils) {
        this.userService = userService;
        this.patchUtils = patchUtils;
    }

    //Endpoint para crear el usuario en el registro y guardarlo en la BD
    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody @Valid User user) { //Valid para que se apliquen las restricciones de la clase User

        //Comprobamos primero que en la BD no exista un user igual, teniendo en cuenta el email
        if (userService.getUser(user.getEmail()) != null){
            //En este caso el recurso ya existe por lo que devolvemos el código de conflicto
            return ResponseEntity.status(409).body("El usuario ya existe");
        }

        //En caso de que no exista podemos llamar a la función de guardar el user en la , guardamos el resultado que es un User en el DTO correspondiente para enseñar solo la info necesaria y evitar mostrar info sensible en la API
        User userRecuperado = userService.registerUser(user);
        UserDTO.UserResponseDTO userRecuperadoDTO = new UserDTO.UserResponseDTO(userRecuperado);

        //Creamos el mapa para enseñar la info que se ha creado en el endpoint si se ha ejecutado con éxito
        Map<String, Object> mapResponse = Map.of(
            "message", "User created successfully",
            "Resource", userRecuperadoDTO
        );
        return ResponseEntity.status(201).body(mapResponse);
    }

    //Endpoint para la actualización de la información de un user
    @PatchMapping("/{email}") //TODO: NO SE SI TENEMOS QUE MIRAR SI LA DATA QUE SE VA ACTUALIZAR TIENE VALORES DISTINTOS A LOS QUE LE PASA EL USER, PQ SI NO LA LLAMADA NO TENDRIA MUCHO SENTIDO?
    //Spring ya convierte el String de la URL a un Long en este caso
    public ResponseEntity<?> updateUser(@PathVariable("email") String email, @RequestBody List<Map<String, Object>> updates){
        try {
            //Recuperamos el user de la BD y vemos si existe o no
            User user = userService.getUser(email);
            if (user == null){
                return ResponseEntity.status(404).body("El usuario no existe");
            }

            //En caso de que el user exista comprobamos que en la lista de operaciones no haya ningun path de un atributo no modificable
            //Primero hacemos una lista de los paths que no se pueden modificar
            List<String> pathsNoModificables = List.of("/id", "/name","/age", "/role");

            for (Map<String,Object> update : updates) {
                String path = (String) update.get("path");
                //En caso de que el path de la operación que quiere hacer el user no este permitida devolvemos Bad Request
                if (pathsNoModificables.contains(path)){
                    HashMap<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("Error", "No se puede modificar el campo "+ path);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                //Hacemos caso especial para el caso de que el user cambie la contraseña de la cuenta, la recibimos en texto plano y tenemos que encriptarla
                else if(path.equals("/password")){
                    String rawPassword = (String) update.get("value");
                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    String encodedPassword = passwordEncoder.encode(rawPassword);
                    update.put("value", encodedPassword); //Actualizamos el valor de la contraseña que vamos a aplicar en el patch
                }
            }

            System.out.println("Datos antes de pasar a PatchUtils: " + user.getClass().getName());
            //Una vez comprobado los atributos que va a modificar el user, llamamos a patchUtils
            User userActualizado = patchUtils.patch(user, updates);
            System.out.println("USER ACTUALIZADO: "+ userActualizado);
            //Guardamos el user actualizado en la BD, el que nos devuelve el service lo transformamos al DTO correspondiente para mostrar solo la info necesaria
            UserDTO.UserUpdateDTO updateUserDTO = new UserDTO.UserUpdateDTO(userService.updateUser(userActualizado));
            //Devolvemos ok y la info actualizada
            return ResponseEntity.ok(updateUserDTO);
            
        } catch (JsonPatchException e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al aplicar el patch "+ e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        }
    

    }

    //Endpoint para obtener la info de un user en base a su email
    @GetMapping("/{email}")
    public ResponseEntity<?> getUser(@PathVariable("email") String email){
        //llamamos a la función que recupera el user de la BD y comprobamos que exista
        User user = userService.getUser(email);

        if (user==null) return ResponseEntity.status(404).body("User not found");
        //En caso de que si que exista y hayamos recuperado su info, la pasamos al DTO correspondiente para no mostrar toda la info de la entidad en la respuesta
        UserDTO.UserResponseDTO userDTO = new UserDTO.UserResponseDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    //TODO: REPASARLOS
    //Endpoint para recuperar los tips favoritos de un user
    @GetMapping("/{email}/favorites")
    public ResponseEntity<List<TipDTO>> getFavoritesTips(@PathVariable("email") String email){
        List<TipDTO> favoriteTips = userService.getFavoritesTips(email);
        if (favoriteTips.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(favoriteTips);
    }

    //Endpoint para eliminar un tip de los favoritos de un user
    @DeleteMapping("/{id}/favorites/{idTip}")
    public ResponseEntity<TipDTO> deleteFavoriteTip(@PathVariable("id") Long id, @PathVariable("idTip") Long idTip){
        //llamamos a la función del service que se encarga de esta lógica
        TipDTO tipDTO = userService.deleteFavoriteTip(id, idTip);
        if (tipDTO == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(tipDTO);
    }

    //Endpoint para añadir un tip a los favoritos de un user
    @PostMapping("/{id}/favorites/{idTip}")
    public ResponseEntity<TipDTO> addFavoriteTip(@PathVariable("id") Long id, @PathVariable("idTip") Long idTip){
        //Llamamos a la función del service que se encarga de esta lógica
        TipDTO tipDTO = userService.addFavoriteTip(id, idTip);
        if (tipDTO == null) return ResponseEntity.notFound().build();
        return ResponseEntity.status(HttpStatus.CREATED).body(tipDTO); //Guardado correctamente en la lista de favoritos del user
    }








    //TODO: MOVER AL LADO DEL CONTROLLER DE SOUNDS

    //TODO: ENDPOINT PARA OBTENER LOS SONIDOS QUE EL USER HA SUBIDO A LA APP, SOLO PUEDEN LLAMARLO LOS DUEÑOS DEL AUDIO
    //Endpoint para obtener los sonidos de los users 
    @GetMapping("/{id}/sounds")
    public ResponseEntity<List<SoundDTO>> getUserSounds(@PathVariable("id") Long id ){
        List<SoundDTO> userSounds = userService.getUserSounds(id);
        if(userSounds.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(userSounds);
    }

    //Endpoint para que el user pueda crear un sonido
    @PostMapping("/{id}/sounds")
    public ResponseEntity<SoundDTO> createSound(@PathVariable("id") Long id, @RequestBody @Valid Sound sound){
        //llamamos a la función encargada de crear el sonido
        SoundDTO createdSound = userService.createSound(id, sound);
        if(createdSound == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(createdSound);
    }

    //TODO: HACER EL ENDPOINT DE ELIMINAR UN SONIDO SI EL USUARIO YA NO LO QUIERE
    @DeleteMapping("/{id}/sounds/{idSound}")
    public ResponseEntity<SoundDTO> deleteSoundUser(@PathVariable("id") Long idUser, @PathVariable("idSound") Long idSound){
        //llamamos a la función que se encarga de esta lógica
        SoundDTO soundDTO = userService.deleteSoundUser(idUser, idSound);
        if (soundDTO == null) return ResponseEntity.notFound().build(); //No se ha encontrado el sonido o el user
        return ResponseEntity.ok(soundDTO);
    }


}
