package com.api.api.DTO;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import com.api.api.model.Chat;

import lombok.Getter;
import lombok.Setter;



public class ChatResponseDTO {

    /*
         * Info del chat que queremos mostrar, dependiendo del contexto en algunos lados será la info del chat,
         *  en otros la respuesta de la IA y cuando se crea el chat será la info del chat y el mensaje devuelto por la IA
         */

         @Getter @Setter
        public static class ChatDetailsDTO {
            private Long id;
            private String name;
            private LocalDateTime date;
            private boolean isEditable = false; //por defecto no es editable, solo lo será si el chat es del día de hoy
            
            public ChatDetailsDTO(Chat chat) {
                this.id = chat.getId();
                this.name = chat.getName();
                this.date = chat.getDate();
            }

            //Constructor para indicar que el chat es de hoy y por lo tanto se puede escribir en el
            public ChatDetailsDTO(Chat chat, boolean isEditable) {
                this.id = chat.getId();
                this.name = chat.getName();
                this.date = chat.getDate();
                this.isEditable = isEditable;
            }
        }

        @Getter @Setter
        public static class IAResponseDTO implements ChatResponse{
            //mensaje recibido por la IA
            private String response;
            
            public IAResponseDTO(String response) {
                this.response = response;
            }
        }

        @Getter @Setter
        public static class ChatCreatedDTO implements ChatResponse{
            private Long id;
            private String name;
            private LocalDateTime date;
            private String response;
            
            public ChatCreatedDTO(Chat chat, String response) {
                this.id = chat.getId();
                this.name = chat.getName();
                this.date = chat.getDate();
                this.response = response;
            }
        }

        @Getter @Setter
        public static class ChatDeletedDTO{
            private Long id;
            private String name;
            
            public ChatDeletedDTO(Chat chat) {
                this.id = chat.getId();
                this.name = chat.getName();
            }
        }

}
