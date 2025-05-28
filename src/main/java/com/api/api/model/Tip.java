package com.api.api.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "tips", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"title", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título no puede ser vacío")
    @Size(min = 2, message = "El título debe tener al menos 2 caracteres")
    @Column(nullable = false, unique = true) //Cualquier tip tiene que tener un nombre único dentro de los tips que tiene el user
    private String title;

    @NotBlank(message = "La descripción no puede ser vacía")
    @Column(nullable = false)
    private String description;

    @NotBlank(message = "El icono no puede ser vacío")
    @Column(nullable = false)
    private String icon;

    @NotBlank(message = "El color no puede ser vacío")
    @Column(nullable = false)
    private String color; //Color del icono de el tip

    //Campo de la entidad que nos indica si el user tiene el tip como favorito o no
    @Column(nullable = false)
    @JsonProperty("isFavorite") //Indicamos a Spring como tiene que devolver el nombre de este atributo en el JSON
    private boolean isFavorite = false; //Por defecto, el tip no es favorito

    //Se llenara el campo gracias a la función que hemos creado en el prePersist
    @Column(nullable = false)
    private LocalDateTime timeStamp;

    //DEFINIMOS LAS RELACIONES QUE TIENE LA ENTIDAD TIP CON EL RESTO DE ENTIDADES DE NUESTRA BD

    /*La relacion muchos a muchos entre user y tips ya esta definida en la entidad User, por lo que para conectarla
     desde este lado tenemos que hacer una mappedBy al atributo que representa dicha relacion en la clase User
     @ManyToMany(mappedBy = "favoriteTips")
     @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
     private Set<User> users; //Lista de users que han marcado este tip como favorito*/

     //Relación uno a muchos entre los tips y el user
     @ManyToOne
     @JoinColumn(name = "user_id")
     @JsonBackReference(value = "userTips")
     private User user;

     /*Relación uno a uno entre un tip y sus detalles*/
     @OneToOne(mappedBy = "tip", cascade = CascadeType.ALL) //Entiendo que lo que indica esto es que todo lo que se haga de cambios en la entidad, se reflejará en la otra que está relacionada
     @JsonManagedReference(value = "tipDetails")
     private TipDetail tipDetail;


     @PrePersist
     protected void onCreate(){
        this.timeStamp = LocalDateTime.now();
     }

     //DEFINIMOS EL MÉTODO EQUALS Y HASHCODE PARA QUE SE PUEDAN COMPARAR DOS OBJETOS DE LA CLASE TIP
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        //Dos tips son iguales si tienen el mismo título, descripción e icono
        return title.equals(tip.title) &&
        description.equals(tip.description) &&
        icon.equals(tip.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, icon); //Devuelve un hash del objeto tip
    }
}
