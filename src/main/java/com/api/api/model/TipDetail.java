package com.api.api.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tip_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La descripción no puede ser vacía")
    @Column(nullable = false)
    private String fullDescription;

    @NotBlank(message = "Los beneficios no pueden ser vacíos")
    @Column(nullable = false)
    private List<String> benefits;

    @NotBlank(message = "Los pasos no pueden ser vacíos")
    @Column(nullable = false)
    private List<String> steps;

    //DEFINIMOS LAS RELACIONES QUE TIENE LA ENTIDAD TIPDETAIL CON EL RESTO DE ENTIDADES DE NUESTRA BD, en este caso solo con Tip "padre"
    @OneToOne
    @JoinColumn(name = "tip_id") //Clave foránea que conecta con la entidad Tip
    private Tip tip;
}
