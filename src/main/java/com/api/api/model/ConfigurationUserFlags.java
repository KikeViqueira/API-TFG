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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "configuration_user_flags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"flagKey", "userId"})
})
/*
 * De esta manera nos aseguramos que el valor de flagKey sea único para cada user
 * lo cual no implica que el flagKey sea único globalmente en los registros de toda la tabla
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ConfigurationUserFlags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 55) //El flagKey es único para cada user (controlado por la constraint de tabla)
    private String flagKey;

    @Column(nullable = true, length = 55)
    private String flagValue;

    @Column(nullable = false)
    private LocalDateTime timeStamp;

    //TENEMOS QUE DEFINIR LA RELACIÓN DE LA ENTIDAD CON LA ENTIDAD DE USER
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @JsonBackReference(value = "user-configurationUserFlags")
    private User user;

    @PrePersist
    protected void onCreate(){
        this.timeStamp = LocalDateTime.now();
    }

}
