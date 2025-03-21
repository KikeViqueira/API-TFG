package com.api.api.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.api.api.DTO.ChatResponseDTO;
import com.api.api.DTO.TipDTO;
import com.api.api.DTO.UserDTO;
import com.api.api.DTO.UserDTO.UserResponseDTO;
import com.api.api.DTO.UserDTO.UserUpdateDTO;
import com.api.api.model.Message;
import com.api.api.model.User;
import com.api.api.service.PatchUtils;
import com.api.api.service.UserService;
import com.github.fge.jsonpatch.JsonPatchException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    //Definimos el constructor de la clase
    @Autowired //Inyección de dependencias en el constructor de la clase
    public UserController(UserService userService, PatchUtils patchUtils) {
        this.userService = userService;
    }

    //Endpoint para crear el usuario en el registro y guardarlo en la BD
    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody @Valid User user) { //Valid para que se apliquen las restricciones de la clase User
        //llamamos a la función que se encarga de registrar el user en la BD
        UserDTO.UserResponseDTO userResponseDTO = userService.registerUser(user);
        //Creamos el mapa para enseñar la info que se ha creado en el endpoint si se ha ejecutado con éxito
        Map<String, Object> mapResponse = Map.of(
            "message", "User created successfully",
            "Resource", userResponseDTO
        );
        return ResponseEntity.status(201).body(mapResponse);
    }

    //Endpoint para la actualización de la información de un user
    @PatchMapping("/{idUser}") //TODO: NO SE SI TENEMOS QUE MIRAR SI LA DATA QUE SE VA ACTUALIZAR TIENE VALORES DISTINTOS A LOS QUE LE PASA EL USER, PQ SI NO LA LLAMADA NO TENDRIA MUCHO SENTIDO?
    public ResponseEntity<UserUpdateDTO> updateUser(@PathVariable("idUser") Long idUser, @RequestBody List<Map<String, Object>> updates) throws JsonPatchException {
        //llamamos a la función que se encarga de actualizar la info del user
        UserUpdateDTO userUpdateDTO = userService.updateUser(idUser, updates);
        return ResponseEntity.ok(userUpdateDTO);
    }

    //Endpoint para obtener la info de un user en base a su email
    @GetMapping("/{idUser}")
    public ResponseEntity<UserDTO.UserResponseDTO> getUser(@PathVariable("idUser") Long idUser){
        //llamamos a la función que recupera el user de la BD y comprobamos que exista
        User user = userService.getUser(idUser);
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

    //TODO: REPASARLOS
    //Endpoint para recuperar los tips favoritos de un user
    @GetMapping("/{email}/favorites")
    public ResponseEntity<List<TipDTO.TipFavDTO>> getFavoritesTips(@PathVariable("email") String email){
        List<TipDTO.TipFavDTO> favoriteTips = userService.getFavoritesTips(email);
        return ResponseEntity.ok(favoriteTips);
    }

    //Endpoint para eliminar un tip de los favoritos de un user
    @DeleteMapping("/{id}/favorites/{idTip}")
    public ResponseEntity<TipDTO.TipFavDTO> deleteFavoriteTip(@PathVariable("id") Long id, @PathVariable("idTip") Long idTip){
        //llamamos a la función del service que se encarga de esta lógica
        TipDTO.TipFavDTO tipDTO = userService.deleteFavoriteTip(id, idTip);
        return ResponseEntity.ok(tipDTO);
    }

    //Endpoint para añadir un tip a los favoritos de un user
    @PostMapping("/{id}/favorites/{idTip}")
    public ResponseEntity<TipDTO.TipFavDTO> addFavoriteTip(@PathVariable("id") Long id, @PathVariable("idTip") Long idTip){
        //Llamamos a la función del service que se encarga de esta lógica
        TipDTO.TipFavDTO tipDTO = userService.addFavoriteTip(id, idTip);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipDTO); //Guardado correctamente en la lista de favoritos del user
    }

    //TODO: ENDPOINTS RELACIONADOS CON LOS CHATS QUE PERTENECEN A UN USER (TIENE SENTIDO HACERLOS AQUI YA QUE ESTÁN RELACIONADOS CON LA LÓGICA DEL USER)
    //Endpoint para recuperar el historial de chats de un usuario
    @GetMapping("/{idUser}/chats")
    public ResponseEntity<List<ChatResponseDTO>> getChats(@PathVariable("idUser") Long idUser){
        List<ChatResponseDTO> chats = userService.getChats(idUser);
        return ResponseEntity.ok(chats);
    }

    //Endpoint para eliminar uno o varios chats de un user
    @DeleteMapping("/{idUser}/chats")
    //Recibimos en el cuerpo de la solicitud la lista de los ids de los chats que se quieren eliminar
    public ResponseEntity<List<ChatResponseDTO>> deleteChats(@PathVariable("idUser") Long idUser, @RequestBody List<Long> idChats){
        List<ChatResponseDTO> chats = userService.deleteChats(idUser, idChats);
        return ResponseEntity.ok(chats);
    }

    //Endpoint para cargar la conversación de un chat
    @GetMapping("/{idUser}/chats/{idChat}")
    public ResponseEntity<List<Message>> getChat(@PathVariable("idUser") Long idUser, @PathVariable("idChat") Long idChat){
        List<Message> messages = userService.getChat(idUser, idChat);
        return ResponseEntity.ok(messages);
    }
}
