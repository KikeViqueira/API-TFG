package com.api.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.model.Message;
import com.api.api.repository.MessageRepository;

@Service
public class MessageService {

    private MessageRepository messageRepository;

    //tenemos que tener una instancia de Gemini para enviar los mensajes y obtener respuestas
    private GeminiService geminiService;

    @Autowired
    public MessageService(MessageRepository messageRepository, GeminiService geminiService){
        this.messageRepository = messageRepository;
        this.geminiService = geminiService;
    }

    //Función para mandar un mensaje a la api de gemini como prompt y guardarlo en la bd, asi como la respuesta
    public String sendMessage(Message message){
        //Antes de mandar el mensaje, añadimos los campos en el objeto Message que hemos recibido
        message.setSender("USER");
        messageRepository.save(message);//Guardamos el mensaje en la bd y se guarda automáticamente la relación con el chat al que forma parte
        String response = geminiService.sendMessage(message.getContent());
        //Creamos un nuevo objeto Message para guardar la respuesta de la IA en la BD
        Message responseMessage = new Message();
        responseMessage.setSender("IA");
        //De todo los campos que nos devuelve la api de Gemini, solo nos interesa el campo text
        responseMessage.setContent(response);
        responseMessage.setChat(message.getChat()); //El mensaje estara relacionado con el mismo chat en el que el user ha mandado el mensaje
        messageRepository.save(responseMessage); //Guardamos la respuesta en la bd
        //Devolvemos la respuesta obtenida por la IA
        return response;
    }

    //Función para crear un título en base al contenido del mensaje
    public String createTitle(Message message){
        //Devolvemos la respuesta obtenida por la IA
        return geminiService.createTitle(message.getContent());
    }
    
}
