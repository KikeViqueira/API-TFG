package com.api.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fitbitToken")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FitbitToken {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El accessToken es obligatorio")
    @Column(nullable = false)
    private String accessToken;

    @NotNull(message = "El expiresIn es obligatorio")
    @Column(nullable = false)
    private Long expiresIn;

    @NotBlank(message = "El refreshToken es obligatorio")
    @Column(nullable = false)
    private String refreshToken;

    @NotBlank(message = "El userId es obligatorio")
    @Column(nullable = false)
    private String userIdFitbit; //Id correspondiente al user pero dentro de la BD de Fitbit

    @NotBlank(message = "El tokenType es obligatorio")
    @Column(nullable = false)
    private String tokenType;

    @NotBlank(message = "El scope es obligatorio")
    @Column(nullable = false)
    private String scope;

    /**
     * RELACIONES DE LA ENTIDAD FITBITTOKEN:
     * 
     * SOLO TENEMOS UNA, RELACION UNO A UNO CON EL USER
     */

     @OneToOne
     @JoinColumn(name = "idUser", nullable = false)
     @JsonBackReference(value = "user-fitbitToken")
     private User user; //Usuario al que pertenece el token de Fitbit
}
