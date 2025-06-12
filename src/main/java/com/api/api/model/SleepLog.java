package com.api.api.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

    //TimeStamp es la hora en la que el user se ha levantado para tener los cuestionarios relacionados con su día de una manera correcta
    private LocalDateTime timeStamp;

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
    
}
