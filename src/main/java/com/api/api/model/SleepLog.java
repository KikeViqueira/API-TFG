package com.api.api.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sleep_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SleepLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private ZonedDateTime timeStamp;

    /*
     * RELACIONES QUE INVOLUCRAN A LA ENTIDAD SLEEPLOG:
     * 
     * -> Definimos el lado N de la relación 1 a N con la entidad de User, ya que un user puede tener varios sleeplogs
     * 
     * -> Definimos la relación 1 a N con la entidad de SleepLogsAnswers (en este caso estamos hablando del lado 1), ya que un sleeplog puede tener varias respuestas
    */

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-sleeplogs")
    private User user;

    @OneToOne(mappedBy = "sleepLog", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "sleepLogAnswer")
    //@JsonIgnore
    private SleepLogAnswer sleepLogAnswer;

    //Creamos la función para rellenar el campo de fecha automáticamente cuando la entidad vaya a ser guardada en la BD
    @PrePersist
    protected void onCreate(){
        this.timeStamp = ZonedDateTime.now(ZoneId.systemDefault()); //Conseguimos la hora y fecha en la zona horaria en la que está el user
    }
    
}
