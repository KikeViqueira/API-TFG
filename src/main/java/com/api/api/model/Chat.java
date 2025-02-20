package com.api.api.model;


import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La fecha del chat no puede ser vacía")
    @Column(nullable = false)
    private LocalDate date;

    /*DEFINIMOS LAS RELACIONES DE LA CLASE*/

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = false)
    private User user; //Usuario que ha creado el chat en la app

    //Relación uno a muchos ya que un chat puede tener muchos mensajes
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL) //Si eliminamos el chat tenemos que eliminar todos los mensajes pertenecientes a el
    private List<Message> messages; //Lista de mensajes que pertenecen al chat
}
