package com.api.api.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_user_flags")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class DailyUserFlags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 55)
    private String flagKey;

    @Column(nullable = false, length = 55)
    private String flagValue;

    @Column(nullable = false)
    private LocalDateTime timeStamp;

    /*
     * Atributo que solo será usado en principio por la bandera que guarda a que hora se ha ido a dormir el user
     * Ya que esta puede estar activa hasta pasadas 24 horas, en el futuro si se añaden más que sigan esta lógica pues tenemos una 
     * solución escalable
     * 
     * En el caso de las banderas que en si son diarias no hace flata rellenar este campo ya que por la naturaleza de ellas
     * el timeStamp ya nos dice en que día se ha creado y por lo tanto cuando busquemos en la BD por día pues no nos
     * traerá resultados en el caso de estar en el día siguiente
     */
    @Column(nullable = true)
    private LocalDateTime expiryTime;

    //TENEMOS QUE DEFINIR LA RELACIÓN DE LA ENTIDAD CON LA ENTIDAD DE USER
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @JsonBackReference(value = "user-dailyUserFlags")
    private User user;

    @PrePersist
    protected void onCreate(){
        this.timeStamp = LocalDateTime.now();
    }

}
