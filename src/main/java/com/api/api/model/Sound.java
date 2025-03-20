package com.api.api.model;


import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="sounds")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sound {

    /*
     * Al tener solo una relación y tener esta la etiqueta @JsonBackReference, no es neceario hacer un DTO para evitar el lazy loading
     * ya que en este caso al serializar el objeto nunca se va a tener en cuenta el campo de la relación en este caso owner
     * 
     * Aún así, hacemos dos DTOs en un formato adecuado para que cuando sea recibido por el frontend no haya problemas
     */

    //Atrubutos de la entidad Sound
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del sonido no puede ser vacío")
    @Size(min = 2, message = "El nombre del sonido debe tener al menos 2 caracteres")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "La URL del archivo no puede ser vacía")
    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private boolean isDefault;

    /*Definimos las relaciones que tiene la entidad con user:
     * 1. Un sonido subido a la app siempre se corresponde con un solo user, y el user puede tener varios sonidos
    */

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = true) //Indicamos que el valor de la columna puede ser null, este caso es para cuando no existe relación con un user
    @JsonBackReference(value = "user-sounds")
    private User owner; //Dueño del sonido
    //Tenemos que definir los endpoints de este lado de la relación ya que es la que tiene el join



    //DEFINIMOS EL MÉTODO EQUALS Y HASHCODE PARA QUE SE PUEDAN COMPARAR DOS OBJETOS DE LA CLASE SOUND
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sound sound = (Sound) o;
        //Dos sonidos son iguales si tienen la misma URL
        return fileUrl.equals(sound.fileUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileUrl); //Devuelve un hash del objeto tip
    }
}
