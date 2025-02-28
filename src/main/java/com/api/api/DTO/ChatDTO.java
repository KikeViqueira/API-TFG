package com.api.api.DTO;

import java.time.ZonedDateTime;

import com.api.api.model.Chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDTO {

    private Long id;
    private String name;
    private ZonedDateTime date;

    public ChatDTO(Chat chat) {
        this.id = chat.getId();
        this.name = chat.getName();
        this.date = chat.getDate();
    }
    
}
