package com.api.api.model;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sleep_log_answer")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SleepLogAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La hora en la que el user se ha ido a dormir no puede ser vacía")
    @Column(nullable = false)
    private ZonedDateTime SleepTime;

    @NotBlank(message = "La hora en la que el user se ha despertado no puede ser vacía")
    @Column(nullable = false)
    private ZonedDateTime WakeUpTime;

    @NotBlank(message = "La duración del sueño no puede ser vacía")
    @Column(nullable = false)
    private float duration;

    @NotBlank(message = "La respuesta a la pregunta 1 no puede ser vacía")
    @Column(nullable = false)
    private String Answer1;

    @NotBlank(message = "La respuesta a la pregunta 2 no puede ser vacía")
    @Column(nullable = false)
    private String Answer2;

    /*
     * RELACIONES QUE INVOLUCRAN A LA ENTIDAD SLEEPLOGANSWER:
     * 
     * -> Definimos la relación 1 a 1 con la entidad de SleepLog, ya que un sleeplog solo tiene una respuesta
     */

     @OneToOne
     @JoinColumn(name = "sleepLog_id", nullable = false)
     @JsonIgnore
     @JsonManagedReference
     private SleepLog sleepLog;
    
}
