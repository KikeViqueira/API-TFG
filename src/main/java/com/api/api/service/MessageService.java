package com.api.api.service;

import com.api.api.DTO.MessageDTO;
import com.api.api.model.GeminiResponse;
import com.api.api.model.Message;
import com.api.api.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class MessageService {

    private MessageRepository messageRepository;

    //tenemos que tener una instancia de Gemini para enviar los mensajes y obtener respuestas
    private GeminiService geminiService;

    private ObjectMapper objectMapper;

    @Autowired
    public MessageService(MessageRepository messageRepository, GeminiService geminiService, ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    //Función para mandar un mensaje a la api de gemini como prompt y guardarlo en la bd, asi como la respuesta
    public String sendMessage(Message message, Long idChat) {
        /*
         * Primero lo que hemos de hacer es recuperar los mensajes de la conversación que se han guardado en la BD entre user y chat
         *
         * De esta manera cuando vayamos a llamar a la función de enviar mensaje del servicio de gemini, le pasaremos como prompt el mensaje que está enviando
         * el user ahora mismo, junto a la conversación que ha habido hasta ahora para que la ia tenga el contexto de lo que estamos hablando
         *
         * */
        List<MessageDTO> messageDTOs = getContextForChat(idChat);
        //Creamos un String para guardar la respuesta de la IA
        String responseText = null;
        //Antes de mandar el mensaje, añadimos los campos en el objeto Message que hemos recibido
        message.setSender("USER");
        this.messageRepository.save(message);//Guardamos el mensaje en la bd y se guarda automáticamente la relación con el chat al que forma parte
        /*
         * Mandamos el mensaje actual del user ala IA más la conversación que ha habido hasta ahora
         * Dependiendo de si hay mensajes hasta ahora o no
         */
        String response = null;
        if (Objects.nonNull(messageDTOs) && !messageDTOs.isEmpty())
            response = this.geminiService.sendMessage(message.getContent() + ", los mensajes que ha habido hasta ahora (un valor del ID menor significa que el mensaje es anterior a los que tienen ID superior) para que entienda el contexto son: " + messageDTOs.toString());
        else response = this.geminiService.sendMessage(message.getContent());

        //Creamos un nuevo objeto Message para guardar la respuesta de la IA en la BD
        Message responseMessage = new Message();
        responseMessage.setSender("IA");
        /*
         *De la respuesta que nos devuelve la api de Gemini, solo nos interesa el campo text
         *Para eso pasamos el JSON que hemos recibido a un objeto de la clase GeminiResponse mediante el uso de ObjectMapper de Jackson
         */
        try {
            GeminiResponse geminiResponse = this.objectMapper.readValue(response, GeminiResponse.class);
            //Guardamos en un String la parte de la respuesta de la IA que nos interesa
            responseText = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
            responseMessage.setContent(responseText);
            responseMessage.setChat(message.getChat()); //El mensaje estara relacionado con el mismo chat en el que el user ha mandado el mensaje
            this.messageRepository.save(responseMessage); //Guardamos la respuesta en la bd
        } catch (JsonProcessingException e) {
            System.out.println("Error al pasar la respuesta de la API de la IA al objeto correspondiente: " + e.getMessage());
        }
        //Devolvemos la respuesta obtenida por la IA
        return responseText;
    }

    //Función para crear un título en base al contenido del mensaje
    public String createTitle(Message message) {
        //Devolvemos la respuesta obtenida por la IA
        String responseText = null;
        String response = this.geminiService.createTitle(message.getContent());
        try {
            GeminiResponse geminiResponse = this.objectMapper.readValue(response, GeminiResponse.class);
            //Guardamos en un String la parte de la respuesta de la IA que nos interesa
            responseText = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
        } catch (JsonProcessingException e) {
            System.out.println("Error al pasar la respuesta de la API de la IA al objeto correspondiente: " + e.getMessage());
        }
        return responseText;
    }

    /*
     * Función para obtener todos los mensajes de una conversación entre un user y un chat hasta el momento
     *
     * Solo hace falta que pasemos el id del chat ya que en la función del chatService ya hemos comprobado que existe la relación entre el user y el chat
     */
    public List<MessageDTO> getContextForChat(Long idChat) {
        List<MessageDTO> messageDTOs = new ArrayList<>();
        List<Message> messages = this.messageRepository.findByChat_IdOrderByIdAsc(idChat);
        if (!messages.isEmpty()) {
            for (Message message : messages) messageDTOs.add(new MessageDTO(message));
        }
        return messageDTOs; //Devolvemos la lista vacía ya que por ahora no hay mensajes para poder hacer el contexto
    }

}
