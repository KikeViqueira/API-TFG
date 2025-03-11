package com.api.api.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Se recibe el valor vacío y se asigna el valor en el service del mensaje
    @Column(nullable = false)
    private String sender;

    @NotBlank(message = "El contenido del mensaje no puede ser vacío")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    

    //Lo mismo que en el caso de sender, pero en este caso ya lo añade hibernate
    @Column(nullable = false)
    private ZonedDateTime time; //Tiene en cuenta la zona horaria en la que se encuentra el user

    //DEFINIMOS LAS RELACIONES, EN ESTE CASO SOLO HAY QUE LOS MENSAJES SOLO PERTENECEN A UN CHAT
    @ManyToOne
    @JoinColumn(name = "idChat", nullable = false)
    @JsonBackReference(value = "chat-messages")
    private Chat chat; //Chat al que pertenece el mensaje


    //Función para cuando se cree un mensaje se añada la fecha actual en base a la zona horaria en la que esta el user
    @PrePersist
    public void onCreate(){
        this.time = ZonedDateTime.now(ZoneId.systemDefault()); //Conseguimos la hora y fecha en la zona horaria en la que está el user
    }
}
