package com.api.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.api.api.DTO.ChatResponse;
import com.api.api.model.Message;
import com.api.api.service.ChatService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Chats", description = "Operaciones relacionadas con los chats y mensajes")
@PreAuthorize("hasPermission(#idUser, 'owner')")
@Controller
@RequestMapping("/api/chats")
public class ChatController {

    private ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Endpoint para crear un chat y añadir un mensaje, o añadir mensajes a un chat ya existente.
     * Pasamos el id del chat en la url ("null" si no existe aún en el front), comprobamos si existe, si existe añadimos el mensaje en el registro de la tabla message que esté relacionada con el chat.
     * Si no existe creamos el chat y añadimos el mensaje, que en este caso será el primero del chat.
     */
    @Operation(
        summary = "Añadir mensaje a un chat",
        description = "Crea un chat y añade un mensaje, o añade un mensaje a un chat ya existente. Si el idChat es 'null', se crea un nuevo chat."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Mensaje añadido correctamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario o chat no encontrado", content = @Content)
    })
    @PostMapping("/{idUser}/{idChat}/messages")
    public ResponseEntity<ChatResponse> addMessageToChat(
            @Parameter(description = "ID del usuario", required = true) @PathVariable("idUser") Long idUser,
            @Parameter(description = "ID del chat o 'null' si es nuevo", required = true) @PathVariable("idChat") String idChatStr,
            @Parameter(description = "Mensaje a añadir", required = true) @RequestBody @Valid Message message) {
        //Si el idChat es null, creamos un Long con valor null, si no lo parseamos a Long
        Long idChat = idChatStr.equals("null") ? null : Long.parseLong(idChatStr);
        ChatResponse chatResponseDTO = chatService.addMessageToChat(idUser, idChat, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatResponseDTO);
    } 
}
