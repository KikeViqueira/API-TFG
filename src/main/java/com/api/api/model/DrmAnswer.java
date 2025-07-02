package com.api.api.model;


import com.fasterxml.jackson.annotation.JsonBackReference;

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
@Table(name = "drm_answers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DrmAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La pregunta no puede ser vacía")
    @Column(nullable = false)
    private String question;

    @NotBlank(message = "La respuesta no puede ser vacía")
    @Column(nullable = false)
    private String answer;

    /**
     * RELACIONES DE LA ENTIDAD DRMANSWER:
     * 
     * - EN ESTE CASO ES UNA RELACIÓN MANY TO ONE CON LA ENTIDAD DRM, SIENDO DRMANSWER EL LADO DE MANY
     */

    //Relación con la entidad Drm
    @ManyToOne
    @JoinColumn(name = "drm_id", nullable = false)
    @JsonBackReference(value = "drm-drmAnswer")
    private Drm drm;
    
}
