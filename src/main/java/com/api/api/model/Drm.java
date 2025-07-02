package com.api.api.model;

import java.time.LocalDateTime;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Drm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Se llenara el campo gracias a la función que hemos creado en el prePersist
    @Column(nullable = false)
    private LocalDateTime timeStamp;

    //Atributo que representas el informe que ha generado la IA respecto a la toma de decisiones del user
    @NotBlank(message = "El informe no puede ser vacío")
    /*
     * Tenemos que marcar el campo de manera que sea text, ya que si no el tipo por default que se crea en la BD es varchar de 255 caracteres
     * Lo cual si queremos guardar respuestas superiores a esa longitud no nos serviría
     * 
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String report;

    /**
     * RELACIONES DE LA ENTIDAD DRM:
     * 
     * - EN ESTE CASO ES UNA RELACIÓN ONE TO MANY CON LA ENTIDAD USER, SIENDO DRM EL LADO DE MANY
     * - RELACIÓN ONE TO MANY CON LA ENTIDAD DE DRMANSWER, SIENDO DRM EL LADO DE ONE
     * 
     */

    //Relación con la entidad User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-drm")
    private User user;

    //Relación con la entidad DrmAnswer
    @OneToMany(mappedBy = "drm", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "drm-drmAnswer")
    private List<DrmAnswer> drmAnswers;


    //Funcion para que se llene el campo date automáticamente
    @PrePersist
    protected void onCreate(){
        this.timeStamp = LocalDateTime.now();
    }
    
}
