package com.api.api.model;


import java.time.LocalDateTime;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    //Se llenara el campo gracias a la función que hemos creado en el prePersist
    @Column(nullable = false)
    private LocalDateTime date;

    /*DEFINIMOS LAS RELACIONES DE LA CLASE*/

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = false)
    @JsonBackReference(value = "user-chats")
    private User user; //Usuario que ha creado el chat en la app

    //Relación uno a muchos ya que un chat puede tener muchos mensajes
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL) //Si eliminamos el chat tenemos que eliminar todos los mensajes pertenecientes a el
    @JsonManagedReference(value = "chat-messages")
    @JsonIgnore
    private List<Message> messages; //Lista de mensajes que pertenecen al chat


    //Funcion para que se llene el campo date automáticamente
    @PrePersist
    protected void onCreate(){
        this.date = LocalDateTime.now();
    }
}
