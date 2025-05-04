package com.api.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.api.api.DTO.ChatResponse;
import com.api.api.DTO.UserFlagDTO;
import com.api.api.DTO.ChatResponseDTO.*;
import com.api.api.DTO.UserDTO.*;
import com.api.api.model.User;
import com.api.api.service.ChatService;
import com.api.api.service.UserService;
import com.github.fge.jsonpatch.JsonPatchException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired //Inyección de dependencias
    private UserService userService;

    @Autowired
    private ChatService chatService;

    //Endpoint para crear el usuario en el registro y guardarlo en la BD
    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody @Valid User user) { //Valid para que se apliquen las restricciones de la clase User
        //llamamos a la función que se encarga de registrar el user en la BD
        UserResponseDTO userResponseDTO = this.userService.registerUser(user);
        //Creamos el mapa para enseñar la info que se ha creado en el endpoint si se ha ejecutado con éxito
        Map<String, Object> mapResponse = Map.of(
            "message", "User created successfully",
            "Resource", userResponseDTO
        );
        return ResponseEntity.status(201).body(mapResponse);
    }

    //Endpoint para la actualización de la información de un user
    @PatchMapping("/{idUser}")
    public ResponseEntity<UserUpdateDTO> updateUser(@PathVariable("idUser") Long idUser, @RequestBody List<Map<String, Object>> updates) throws JsonPatchException {
        //llamamos a la función que se encarga de actualizar la info del user
        UserUpdateDTO userUpdateDTO = this.userService.updateUser(idUser, updates);
        return ResponseEntity.ok(userUpdateDTO);
    }

    //Nuevo endpoint para actualizar la foto de perfil del usuario, necisatamos indicar el tipo de datos que se van a recibir mediante el consumes
    @PutMapping(path = "/{idUser}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserUpdateDTO> updateProfilePicture(@PathVariable("idUser") Long idUser, @RequestParam("file") MultipartFile file) {
        UserUpdateDTO userUpdateDTO = this.userService.updateProfilePicture(idUser, file);
        return ResponseEntity.ok(userUpdateDTO);
    }

    //Endpoint para obtener la info de un user en base a su email
    @GetMapping("/{idUser}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable("idUser") Long idUser){
        //llamamos a la función que recupera el user de la BD y comprobamos que exista
        User user = this.userService.getUser(idUser);
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

     /*
     * Funciones que se usan para la gestión de los chats de un user:
     * 1. Recupera el historial de los chats de un user (valor de filter = history)
     * 2. Recupera os chats que ha tenido el user en los últimos tres meses (filter = last3Months)
     * 3. Recupera los chats que hay een un rango de fechas que puede especificar el user (filter = range)
     * 
     * Esto se lo indicamos al método en base a un parámetro que le pasamos en la petición (filter)
     */
    @GetMapping("/{idUser}/chats")
    public ResponseEntity<List<ChatResponse>> getChats(@PathVariable("idUser") Long idUser,
        @RequestParam(name="filter", required = false, defaultValue = "history") String filter,
        @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
        List<ChatResponse> chats = this.chatService.getChats(idUser, filter, startDate, endDate); // Provide appropriate values for the additional parameters
        return ResponseEntity.ok(chats);
    }

    //Endpoint para eliminar uno o varios chats de un user
    @DeleteMapping("/{idUser}/chats")
    //Recibimos en el cuerpo de la solicitud la lista de los ids de los chats que se quieren eliminar
    public ResponseEntity<List<ChatDeletedDTO>> deleteChats(@PathVariable("idUser") Long idUser, @RequestBody List<Long> idChats){
        List<ChatDeletedDTO> chats = this.chatService.deleteChats(idUser, idChats);
        return ResponseEntity.ok(chats);
    }

    //Endpoint para cargar la conversación de un chat
    @GetMapping("/{idUser}/chats/{idChat}")
    public ResponseEntity<ChatMessagesDTO> getChat(@PathVariable("idUser") Long idUser, @PathVariable("idChat") Long idChat){
        ChatMessagesDTO conversation = this.chatService.getChat(idUser, idChat);
        return ResponseEntity.ok(conversation);
    }

    //Endpoint para eliminar la foto de perfil del user
    @DeleteMapping("/{idUser}/profile-picture")
    public ResponseEntity<?> deleteProfilePicture(@PathVariable("idUser") Long idUser){
        this.userService.deleteProfilePicture(idUser);
        return ResponseEntity.noContent().build(); //Indicamos al user que la petición se ha realizado con éxito y no hay contenido para devolver (Es innecesario devolverle la url)
    }

    //Endpoint para obtener la lista de las banderas del user tanto las de configuración como las diarias
    @GetMapping("/{idUser}/flags")
    public ResponseEntity<Map<String, Map<String, String>>> getUserFlags(@PathVariable("idUser") Long idUser){
        Map<String, Map<String, String>> userFlags = this.userService.getUserFlags(idUser);
        return ResponseEntity.ok(userFlags);
    }
}
