package com.api.api.DTO;

import com.api.api.model.User;
import lombok.*;

public class UserDTO {
    /*Usamos un DTO cuando no tenemos que devolver al user toda la info de la entidad o cuando solo
     necesitamos pasar al backend solo ciertas propiedades de la entidad.*/

     //Definimos el DTO que solo tendra los atributos que se devuelven cuando se recupera info del user
     @Getter @Setter
     public static class UserResponseDTO{
        //Atributos que vamos a devolver
        private String name;
        private String email;
        private String profilePicture;

        public UserResponseDTO(User user) {
            this.name = user.getName();
            this.email = user.getEmail();
            if (user.getProfilePicture()!=null) this.profilePicture = user.getProfilePicture();
        }
     }

     //Definimos el DTO que solo tendra los atributos que se podrán actualizar
     @Getter @Setter
     public static class UserUpdateDTO{
        private String email;
        private String profilePicture;
        private String password; //TODO: no se si es bueno devolver la contraseña en el DTO aunque sea un campo que se actualiza


        public UserUpdateDTO(User user) {
            this.email = user.getEmail();
            this.profilePicture = user.getProfilePicture();
            this.password = user.getPassword();
        }
     }


}