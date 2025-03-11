package com.api.api.model;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    //El campo NotBlank solo se aplica a cadenas de texto por lo que para números tenemos que usar NotNull y Min cuando sea necesario

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La hora en la que el user se ha ido a dormir no puede ser vacía")
    @Column(nullable = false)
    private ZonedDateTime SleepTime;

    @NotNull(message = "La hora en la que el user se ha despertado no puede ser vacía")
    @Column(nullable = false)
    private ZonedDateTime WakeUpTime;

    
    @NotNull(message = "La duración no puede ser nula")
    @Min(value = 0, message = "La duración debe ser mayor o igual a 0")
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
     @JsonBackReference(value = "sleepLogAnswer")
     private SleepLog sleepLog;
    
}
