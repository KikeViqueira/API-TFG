package com.api.api.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "El correo debe ser válido")
    @NotBlank(message = "Es necesario que se introduzca un correo")
    private String email;

    @NotBlank(message = "Es necesario que se introduzca una contraseña")
    private String password;
    
}
