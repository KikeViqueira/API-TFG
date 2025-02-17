package com.api.api.model;

import static org.mockito.Mockito.description;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    //TODO: Tenemos que comprobar que el archivo que se subo solo pueda ser de tipo audio (FrontEnd)

    //Atrubutos de la entidad Sound
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String fileUrl;

    private boolean isDefault;

    /*Definimos las relaciones que tiene la entidad con user:
     * 1. Un sonido subido a la app siempre se corresponde con un solo user, y el user puede tener varios sonidos
    */

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = false)
    private User owner; //Dueño del sonido



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
