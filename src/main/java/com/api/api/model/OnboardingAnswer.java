package com.api.api.model;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "onboarding_answer")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La pregunta no puede estar vacía")
    @Column(nullable = false)
    private String question;

    @NotBlank(message = "La respuesta no puede estar vacía")
    @Column(nullable = false)
    private String answer;

    /*
     * RELACIONES DE LA ENTIDAD:
     * 
     * En este caso la única relación que se tiene es con la entidad Onboarding, ya que
     * una respuesta solo puede pertenecer a un onboarding y un onboarding puede tener varias respuestas
     */

     @ManyToOne
     @JoinColumn(name = "onboarding_id", nullable = false)
     @JsonBackReference(value = "onboarding-answers")
     private Onboarding onboarding;

    
}
