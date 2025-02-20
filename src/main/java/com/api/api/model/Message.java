package com.api.api.model;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @NotBlank(message = "Quien ha mandado el mesnsaje no puede ser vacío")
    @Column(nullable = false)
    private String sender;

    @NotBlank(message = "El contenido del mensaje no puede ser vacío")
    @Column(nullable = false)
    private String content;

    @NotBlank(message = "La hora en la que se ha mandado el mensaje no puede ser vacía")
    @Column(nullable = false)
    private ZonedDateTime time; //Tiene en cuenta la zona horaria en la que se encuentra el user

    //DEFINIMOS LAS RELACIONES, EN ESTE CASO SOLO HAY QUE LOS MENSAJES SOLO PERTENECEN A UN CHAT
    @ManyToOne
    @JoinColumn(name = "idChat", nullable = false)
    private Chat chat; //Chat al que pertenece el mensaje
}
