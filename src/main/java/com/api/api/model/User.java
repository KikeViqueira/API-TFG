package com.api.api.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) //Restricciones en la BD
    @NotBlank(message = "El correo no puede ser vacío") //Restriciones en el controller en lo que se refiere a validación
    @Email(message = "El correo debe ser válido") 
    private String email;

    @Column(nullable = false)
    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "El correo no puede ser vacío") //Restriciones en el controller en lo que se refiere a validación
    private String password;


    //Estos atributos no se tieene que completar a la hora de que el user se haga una cuenta en la app

    private int age;
    private String role;
    //Unico campo en la BD que puede ser nulo
    private String profilePicture;


    //DEFINIMOS LAS RELACIONES QUE TIENE LA ENTIDAD USER CON EL RESTO DE ENTIDADES DE NUESTRA BD

    //Un user puede tener varios tips en favoritos, y el mismo tip puede ser el favorito de varios users (Relación muchos a muchos)
    @ManyToMany
    @JoinTable(
        name = "user_favs", //Indicamos el nombre de la tabla intermedia en la BD que representa la relación entre las dos entidades
        joinColumns = @JoinColumn(name="user_id"), //Indicamos el nombre de la columna de la tabla intermedia que hace referencia a la clave primaria de la entidad user
        inverseJoinColumns = @JoinColumn(name="tip_id") //Lo mismo que la anterior pero diciendo el nombre de la columna que hace referencia a la primaria de la tabla con la que se relaciona user (tip)
    )
    @JsonIgnore // TODO: Nunca renderizamos esta info en formato json para evitar la excepcion de lazily initialized
    private List<Tip> favoriteTips; //Lista de tips que el user ha marcado como favoritos

    //Relación uno a muchos entre user y sonidos (Esta es especial solo para los sonidos que ha subido el user)
    //MappedBy hace referencia al campo owner en la entidad Sound (lado Many de la relación)
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL) //Si eliminamos un user, se eliminan los sonidos que el ha subido
    @JsonIgnore
    private List<Sound> soundsUser; //Lista de sonidos que ha subido el user

    //DEFINIMOS EL MÉTODO EQUALS Y HASHCODE PARA QUE SE PUEDAN COMPARAR DOS OBJETOS DE LA CLASE SOUND
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        //Dos sonidos son iguales si tienen la misma URL
        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email); //Devuelve un hash del objeto tip
    }
}