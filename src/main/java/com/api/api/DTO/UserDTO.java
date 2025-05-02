package com.api.api.DTO;

import java.time.LocalDate;

import com.api.api.model.User;
import lombok.*;

public class UserDTO {
    /*Usamos un DTO cuando no tenemos que devolver al user toda la info de la entidad o cuando solo
     necesitamos pasar al backend solo ciertas propiedades de la entidad.*/

     //Definimos el DTO que solo tendra los atributos que se devuelven cuando se recupera info del user
     @Getter @Setter
     public static class UserResponseDTO{
        //Atributos que vamos a devolver
        private Long id;
        private String name;
        private String email;
        private LocalDate birthDate;
        private String profilePicture;
        private int age;

        public UserResponseDTO(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            if(user.getBirthDate()!= null){
                this.birthDate= user.getBirthDate();
                this.age = user.getAge();
            } else this.age = -1;
            if (user.getProfilePicture()!=null) this.profilePicture = user.getProfilePicture();
        }
     }

     //Definimos el DTO que solo tendra los atributos que se podrán actualizar
     @Getter @Setter
     public static class UserUpdateDTO{
        private String newProfilePicture;
        private boolean passwordChanged = false; //Valor por defecto para este campo

        //Constructor para cambiar solo el valor de la foto de perfil
        public UserUpdateDTO(User user) {
            this.newProfilePicture = user.getProfilePicture();
        }

        //Constructor solo para cambiar el valor de la contraseña, se devuelve la foto de perfil que tenga el user en ese momento
        public UserUpdateDTO(User user, boolean passwordChanged) {
            this.newProfilePicture = user.getProfilePicture();
            this.passwordChanged = passwordChanged;
        }
     }


}