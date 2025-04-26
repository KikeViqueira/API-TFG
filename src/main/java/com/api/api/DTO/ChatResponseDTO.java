package com.api.api.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.api.api.model.Chat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;



public class ChatResponseDTO {

    /*
         * Info del chat que queremos mostrar, dependiendo del contexto en algunos lados será la info del chat,
         *  en otros la respuesta de la IA, cuando se crea el chat será la info del chat y el mensaje devuelto por la IA y
         * cuando se elimina un chat
         */

         @Getter @Setter
        public static class ChatDetailsDTO implements ChatResponse{
            private Long id;
            private String name;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            private LocalDateTime date;
            
            public ChatDetailsDTO(Chat chat) {
                this.id = chat.getId();
                this.name = chat.getName();
                this.date = chat.getDate();
            }
        }

        @Getter @Setter
        public static class ChatContributionDTO implements ChatResponse{
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            private LocalDateTime date;
            private Integer count = 1; //Activador para que el día en el que el user ha hecho un chat se vea en el gráfico del perfil
            
            public ChatContributionDTO(Chat chat) {
                this.date = chat.getDate();
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

        /*
        * Clase que nos devuelve la lista de mensajes de un chat, ordenados por el id del mensaje, ya que al ser un chat no es necesario saber la fecha y hora del mensaje
        *  ya que al estar ordenados por id ya sabemos el orden en el que se han enviado
        *
        * Además en el DTO devolvemos la bandera de si el chat es editable o no para que en el FrontEnd sepa si se puede hablar con el chat o no
        */
        @Getter @Setter
        public static class ChatMessagesDTO{
            private List<MessageDTO> messages;
            @JsonProperty("isEditable")
            private boolean isEditable = false; //bandera para saber si el chat es editable o no, si es de hoy es editable, si no es de hoy no es editable

             //Constructor para crear el DTO si el chat no es de hoy
             public ChatMessagesDTO(List<MessageDTO> messages) {
                this.messages = messages;
            }

            //Constructor para crear el DTO si el chat es de hoy
            public ChatMessagesDTO(List<MessageDTO> messages, boolean isEditable) {
                this.messages = messages;
                this.isEditable = isEditable;
            }
        }

}
