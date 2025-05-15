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

    @Column(nullable = false, length = 55, unique = true)
    private String flagKey;

    @Column(nullable = false, length = 55)
    private String flagValue;

    @Column(nullable = false)
    private LocalDateTime timeStamp;

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
