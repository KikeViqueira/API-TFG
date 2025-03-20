package com.api.api.controller;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.api.api.DTO.ChatResponseDTO;
import com.api.api.model.Message;
import com.api.api.service.ChatService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/chats")
public class ChatController {

    private ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /*Endpoint para crear un chat y añadir un mensaje, o añadir mensajes a un chat ya existente
     * Pasamos el id del chat en la url(undefined o null si no existe aún en el front), comprobamos si existe, si existe aañadimos el mensaje en el registro de la tabla message que este relacionada con el chat
     * Si no existe creamos el chat y añadimos el mensaje, que en este caso sera el primero del chat
    */
    @PostMapping("/{idUser}/{idChat}/messages")
    public ResponseEntity<ChatResponseDTO> addMessageToChat(@PathVariable("idUser") Long idUser,@PathVariable("idChat") String idChatStr, @RequestBody @Valid Message message){
        //Si el idChat es null, creamos un Long con valor null, si no lo parseamos a Long
        Long idChat = idChatStr.equals("null") ? null : Long.parseLong(idChatStr);
        ChatResponseDTO chatResponseDTO = chatService.addMessageToChat(idUser, idChat, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatResponseDTO);
    } 
}
