package com.api.api.model;

import java.util.Objects;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "tips")
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
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "La descripción no puede ser vacía")
    @Column(nullable = false)
    private String description;

    @NotBlank(message = "La categoría no puede ser vacía")
    @Column(nullable = false)
    private String icon;

    //DEFINIMOS LAS RELACIONES QUE TIENE LA ENTIDAD TIP CON EL RESTO DE ENTIDADES DE NUESTRA BD

    /*La relacion muchos a muchos entre user y tips ya esta definida en la entidad User, por lo que para conectarla
     desde este lado tenemos que hacer una mappedBy al atributo que representa dicha relacion en la clase User */
     @ManyToMany(mappedBy = "favoriteTips")
     private Set<User> users; //Lista de users que han marcado este tip como favorito

     /*Relación uno a uno entre un tip y sus detalles*/
     @OneToOne(mappedBy = "tip", cascade = CascadeType.ALL) //Entiendo que lo que indica esto es que todo lo que se haga de cambios en la entidad, se reflejará en la otra que está relacionada
     private TipDetail tipDetail;



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
