package com.api.api.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "onboarding")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Onboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Se llenara el campo gracias a la función que hemos creado en el prePersist
    @Column(nullable = false)
    private ZonedDateTime timeStamp;

    /*
     * RELACIONES QUE INVOLUCRAN A LA ENTIDAD ONBOARDING
     * 
     *  Se ha hecho una tabla aparte para las preguntas y respuestas del onboarding para que sea más escalable
     * y así no tener problemas con los arrays en las bases de datos
     * 
     * 
     * orphanRemoval = true indica que si se elimina una entidad hija de la colección en la entidad padre,
     *  Hibernate la borrará automáticamente de la base de datos. Es decir, las entidades "huérfanas" se eliminan sin necesidad de
     *  invocar explícitamente un método de eliminación.
    */

    @OneToMany(mappedBy = "onboarding", cascade = CascadeType.ALL) //Indicamos que la relación es uno a muchos y que el campo que hace referencia a esta entidad en la tabla de la otra entidad es onboarding
    @JsonManagedReference(value = "onboarding-answers")
    //@JsonIgnore
    private List<OnboardingAnswer> answers; 

    /*
     * Relaciones que mantiene Onboarding con User, en este caso es una relación uno a uno
     * ya que un onboarding solo puede pertenecer a un user y un user solo puede tener un onboarding
    */

    @OneToOne
    @JoinColumn(name = "idUser", nullable = false) //Indicamos el nombre de la columna que hace referencia a la clave primaria de la entidad User
    @JsonBackReference(value = "user-onboarding")
    private User user;


    //Funcion para que se llene el campo date automáticamente
    @PrePersist
    protected void onCreate(){
        this.timeStamp = ZonedDateTime.now(ZoneId.systemDefault()); //Conseguimos la hora y fecha en la zona horaria en la que está el user
    }
}
