package com.api.api.DTO;

import com.api.api.model.Message;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageDTO {

    /*
     * Para tener el contexto de la conversación no hace falta saber la hora del mensaje si no que ordenados por id ya nos sirve
     * Así ahorramos token también
     */

    private Long id;

    private String sender;

    private String content;

    public MessageDTO(Message message){
        this.id = message.getId();
        this.sender = message.getSender();
        this.content = message.getContent();
    }

    //Hacemos override de toString para poder imprimir el objeto
    @Override
    public String toString() {
        return  "id=" + id + ", sender='" + sender + '\'' + ", content='" + content + '\'' + '}';
    }
    
}
