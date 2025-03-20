package com.api.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.MethodNotAllowed;
import org.springframework.web.server.MethodNotAllowedException;

import com.api.api.DTO.ChatResponseDTO;
import com.api.api.exceptions.AIResponseGenerationException;
import com.api.api.exceptions.NoContentException;
import com.api.api.model.Chat;
import com.api.api.model.Message;
import com.api.api.model.User;
import com.api.api.repository.ChatRepository;
import com.api.api.repository.MessageRepository;
import com.api.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class ChatService {

    private ChatRepository chatRepository;

    private UserRepository userRepository;

    private MessageService messageService;

    @Autowired
    public ChatService(ChatRepository chatRepository, UserRepository userRepository, MessageService messageService){
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.messageService = messageService;
    }

    //Función para crear un nuevo chat
    public Chat createChat(User user){
        //Al ser una función que llama addMessageToChat, no es necesario comprobar si el user ya existe pq donde llamamos a la función ya hemos hecho la comprobación
        Chat chat = new Chat();
        chat.setUser(user);
        return chat; //Devolvemos el chat a la función que lo ha llamado, pero aún no lo guardamos en la BD ya que tenemos que crearle un título
    }

    @Transactional
    //Función para que el user pueda mandar un mensaje a un chat existente o crear uno nuevo con el primer mensaje que ha enviado
    public ChatResponseDTO addMessageToChat(Long idUser, Long idChat, Message message){
        //tenemos que comprobar si el chat y el user existen
        String response = null;
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (idChat != null){
            //Comprobamos si el chat existe, si no existe lanzamos una excepción
            Chat chat = chatRepository.findById(idChat).orElseThrow(() -> new EntityNotFoundException("Chat no encontrado"));
            //Comprobamos que existe una relación entre el user y el chat
            boolean exits = chatRepository.existsByUserIdAndId(idUser, idChat);
            //Comprobamos si existe relación entre el chat y el user
            if (exits){
                //Comprobamos si el chat es de hoy o no, si no es de hoy no se le puede mandar el mensaje
                if (isChatOfToday(idChat)){
                    message.setChat(chat); //Añadimos el chat al mensaje
                    //Si existe la relación añadimos el mensaje al chat llamando a MessageService
                    response = messageService.sendMessage(message, chat.getId());
                    //Solo nos interesa devolver un ChatResponseDTO con el mensaje que la IA ha generado
                    if (response != null) return new ChatResponseDTO(response);
                    else throw new AIResponseGenerationException("No se ha podido enviar el mensaje");

                } else throw new MethodNotAllowedException("addMessageToChat para chats que no son del día actual.", null);
                
            } else throw new AccessDeniedException("El usuario no tiene acceso a este chat");
        }
        else{
            Chat newChat = createChat(user);
            //Primero tenemos que crear un título para el chat en base al primer mensaje que se ha enviado
            String title = messageService.createTitle(message);
            //Si el título es distinto de null, añadimos la info al chat y lo guardamos y enviamos el mensaje
            if (title != null){
                newChat.setName(title);
                chatRepository.save(newChat); //Ahora si podemos guardar el chat en la BD
                //Una vez creado el chat añadimos el mensaje, y la respuesta que obtenemos de la IA en la tabla de mensajes de la BD
                message.setChat(newChat);
                response = messageService.sendMessage(message, newChat.getId());
                //Nos interesa devolver en el objeto tanto la info del chat que se ha creado como el mensaje que nos ha devuelto la IA
                if (response != null) return new ChatResponseDTO(newChat, response);
                else throw new AIResponseGenerationException("No se ha podido enviar el mensaje");
            } else throw new AIResponseGenerationException("No se ha podido crear un título para el chat");
        }
    }

    //Función privada para saber si el chat es de hoy o no
    private boolean isChatOfToday(Long chatId){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return chatRepository.existsByIdAndDateBetween(chatId, startOfDay, endOfDay);
    }
}
