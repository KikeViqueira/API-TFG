package com.api.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.repository.ChatRepository;

@Service
public class ChatService {

    private ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    //Función para que el user pueda mandar un mensaje al chat
    public String sendMessage(String message){
        
    }
    
}
