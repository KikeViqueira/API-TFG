package com.api.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.MethodNotAllowedException;

import com.api.api.DTO.ChatResponse;
import com.api.api.DTO.MessageDTO;
import com.api.api.DTO.ChatResponseDTO.*;
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

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    //Función para crear un nuevo chat
    public Chat createChat(User user){
        //Al ser una función que llama addMessageToChat, no es necesario comprobar si el user ya existe pq donde llamamos a la función ya hemos hecho la comprobación
        Chat chat = new Chat();
        chat.setUser(user);
        return chat; //Devolvemos el chat a la función que lo ha llamado, pero aún no lo guardamos en la BD ya que tenemos que crearle un título
    }

    @Transactional
    //Función para que el user pueda mandar un mensaje a un chat existente o crear uno nuevo con el primer mensaje que ha enviado
    public ChatResponse addMessageToChat(Long idUser, Long idChat, Message message){
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
                    if (response != null) return new IAResponseDTO(response);
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
                if (response != null) return new ChatCreatedDTO(newChat, response);
                else throw new AIResponseGenerationException("No se ha podido enviar el mensaje");
            } else throw new AIResponseGenerationException("No se ha podido crear un título para el chat");
        }
    }


    /*
     * Funciones que se usan para la gestión de los chats de un user
     */
    //Función para recuperar el historial de chats de un usuario
    @Transactional //Para que no de error al hacer la consulta
    public List<ChatDetailsDTO> getChats(Long idUser){
        //Comprobamos si el user existe en la BD
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Recuperamos todos los chats que están asociados al user que ha hecho la petición
        List<Chat> chatsOfUser = chatRepository.findByUserId(idUser);
        if (chatsOfUser.isEmpty()) throw new NoContentException("El usuario no tiene chats");
        else{
            List<ChatDetailsDTO> chatsRecuperados = new ArrayList<>();
            for (Chat chat : user.getChats()) chatsRecuperados.add(new ChatDetailsDTO(chat));
            return chatsRecuperados;
        }
    }

    //Función para eliminar uno o varios chats de un user
    @Transactional
    public List<ChatDeletedDTO> deleteChats(Long idUser, List<Long> idChats){
        //Comprobamos si el user existe
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Recuperamos la lista de chats que pertenecen al usuario y están en la lista de Ids
        List<Chat> chats = chatRepository.findByUserIdAndIdIn(idUser, idChats);
        if ( chats.size() != idChats.size()) throw new AccessDeniedException("Uno o más chats no pertenecen al usuario");
        //Si no se ha disparado la excepción podemos eliminar los chats tanto de la entidad del user como de la BD
        user.getChats().removeAll(chats);
        userRepository.save(user);
        chatRepository.deleteAll(chats);
        List<ChatDeletedDTO> chatsRecuperados = new ArrayList<>();
        for (Chat chat : chats) chatsRecuperados.add(new ChatDeletedDTO(chat));
        return chatsRecuperados;
    }

    //Función para recuperar la conversación de un chat
    @Transactional
    public ChatMessagesDTO getChat(Long idUser, Long idChat){
        //Comprobamos si el user existe
        userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Comprobamos si el chat existe
        chatRepository.findById(idChat).orElseThrow(() -> new EntityNotFoundException("Chat no encontrado"));
        //Comprobamos si existe una relación entre el user y el chat
        if (chatRepository.existsByUserIdAndId(idUser, idChat)){
            //Comprobamos si el chat es de hoy o no
            boolean isChatOfToday = isChatOfToday(idChat);
            //Recuperamos la lista de mensajes del chat llamando a la función del repositorio de mensajes que nos los devuelve ya de manera ordenada (ascendiente)
            List<Message> messages = messageRepository.findByChat_IdOrderByIdAsc(idChat);
            //Tenemos que pasar estos mensajes a su correspondiente DTO
            List<MessageDTO> messageDTOs = new ArrayList<>();
            for (Message message : messages) messageDTOs.add(new MessageDTO(message));
            //Dependiendo de si el chat es de hoy o no, tenemos que indicar de manera correcta el valor de la bandera isEditable del DTO que vamos a devolver
            if (isChatOfToday) return new ChatMessagesDTO(messageDTOs, true);
            else return new ChatMessagesDTO(messageDTOs);
        } else throw new IllegalArgumentException("El usuario no tiene acceso a este chat");
    }

    //Función privada para saber si el chat es de hoy o no
    private boolean isChatOfToday(Long chatId){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return chatRepository.existsByIdAndDateBetween(chatId, startOfDay, endOfDay);
    }
}
