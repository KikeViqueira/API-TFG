package com.api.api.DTO;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import com.api.api.model.Chat;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class ChatResponseDTO {

    /*
         * Info del chat que queremos mostrar, dependiendo del contexto en algunos lados será la info del chat,
         *  en otros la respuesta de la IA y cuando se crea el chat será la info del chat y el mensaje devuelto por la IA
         */
        private Long id;
        private String name;
        private LocalDateTime date;

        //mensaje recibido por la IA
        private String response;
    
        public ChatResponseDTO(Chat chat) {
            this.id = chat.getId();
            this.name = chat.getName();
            this.date = chat.getDate();
        }

        public ChatResponseDTO(String response){
            this.response = response;
        }

        public ChatResponseDTO(Chat chat, String response){
            this.id = chat.getId();
            this.name = chat.getName();
            this.date = chat.getDate();
            this.response = response;
        }

}
