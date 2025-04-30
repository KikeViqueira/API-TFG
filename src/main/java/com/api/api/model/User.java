package com.api.api.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
/*
 * Lado dueño vs. lado inverso:
 * - El lado dueño (sin 'mappedBy') es el que se encarga de persistir la relación (guarda la FK en la BD).
 * - El lado inverso (con 'mappedBy') solo refleja la relación.
 * 
 * Para una persistencia correcta, asigna la relación en el lado dueño. 
 * Se recomienda sincronizar ambos lados (ej.: padre.setHijo(hijo) y hijo.setPadre(padre)) 
 * para mantener la consistencia en la lógica y en la serialización.
 * 
 * pero si es para guardar solo en la BD, solo se tiene que hacer en el lado dueño
 * 
 * Nota: @JsonBackReference se usa para evitar ciclos en la serialización JSON y no afecta la persistencia.
 */

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
    @NotBlank(message = "El nombre no puede ser vacío") //Restriciones en el controller en lo que se refiere a validación
    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "El correo no puede ser vacío") //Restriciones en el controller en lo que se refiere a validación
    private String password;

    //Este atributo se completará cuando el user haga el onboarding, por lo que puede ser nulo al crear la entidad que representa al user
    @Column(nullable = true)
    private LocalDate birthDate;

    private String role;

    //Unico campo en la BD que puede ser nulo
    private String profilePicture;

    //Campo que puede ser nulo  que se encargará de guardar el public_id que nos devuelve Cloudinary en el momento que el user sube una imagen de perfil, si es null se supone que de foto de perfil está el placeholder
    @Column(nullable = true, length = 255, name = "public_id")
    private String publicIdCloudinary;


    /*
     * DEFINIMOS LAS RELACIONES QUE TIENE LA ENTIDAD USER CON EL RESTO DE ENTIDADES DE NUESTRA BD
     * 
     * Usa @JsonManagedReference en el lado "padre" (el que se serializa, por ejemplo, la colección).
     * Usa @JsonBackReference en el lado "hijo" (el que tiene @JoinColumn).
     * Asegúrate de usar el mismo identificador en ambos lados (ej: "parent-child").
     * 
     * Si no queremos que al serializar se tenga en cuenta el campo en el lado del @JsonManagedReference tenemos que usar @JsonIgnore
     * Por ahora devolveremos todo aunque desués en realidad lo que devolvemos es el DTO
     * 
     * @JsonIdentityInfo permite manejar relaciones Many-to-Many evitando ciclos de serialización.
     * Al usar un generador de identificadores (por ejemplo, el valor del campo "id"),
     * Jackson serializa cada objeto una única vez y, en apariciones posteriores, solo usa el id para referenciarlo.
     * Esto previene la recursión infinita en relaciones bidireccionales.
     * 
    Un user puede tener varios tips en favoritos, y el mismo tip puede ser el favorito de varios users (Relación muchos a muchos)
    @ManyToMany
    @JoinTable(
        name = "user_favs", //Indicamos el nombre de la tabla intermedia en la BD que representa la relación entre las dos entidades
        joinColumns = @JoinColumn(name="user_id"), //Indicamos el nombre de la columna de la tabla intermedia que hace referencia a la clave primaria de la entidad user
        inverseJoinColumns = @JoinColumn(name="tip_id") //Lo mismo que la anterior pero diciendo el nombre de la columna que hace referencia a la primaria de la tabla con la que se relaciona user (tip)
    )
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIgnore
    private List<Tip> favoriteTips; //Lista de tips que el user ha marcado como favoritos

     * */

     //Relación uno a muchos entre user y tips (Un user puede tener varios tips, pero un tip solo puede pertenecer a un user)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) //Si eliminamos un user, se eliminan los tips que el ha subido
    @JsonManagedReference(value = "userTips")
    private List<Tip> tips; //Lista de tips que ha subido el user

    //Relación uno a muchos entre user y sonidos (Esta es especial solo para los sonidos que ha subido el user)
    //MappedBy hace referencia al campo owner en la entidad Sound (lado Many de la relación)
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL) //Si eliminamos un user, se eliminan los sonidos que el ha subido
    @JsonManagedReference(value = "user-sounds")
    //@JsonIgnore
    private List<Sound> soundsUser; //Lista de sonidos que ha subido el user

    //Relación uno a muchos entre user y chat
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-chats")
    //@JsonIgnore
    private List<Chat> chats; //Lista de chats que ha creado el user

    //Relación uno a uno entre user y onboarding
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-onboarding")
    //@JsonIgnore
    private Onboarding onboarding; //Onboarding del user

    //Relación uno a muchos entre user y sleeplogs
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-sleeplogs")
    //@JsonIgnore
    private List<SleepLog> sleepLogs;

    //Relación uno a uno entre user y fitbitToken
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-fitbitToken")
    //@JsonIgnore
    private FitbitToken fitbitToken; //Token de Fitbit del user

    //relación uno a muchos entre user y drm
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-drm")
    //@JsonIgnore
    private List<Drm> drms;


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

     /*
     * Calcula la edad del usuario basándose en su fecha de nacimiento.
     * Retorna -1 si birthDate es null.
     */
    public int getAge() {
        if (birthDate == null) return -1;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}