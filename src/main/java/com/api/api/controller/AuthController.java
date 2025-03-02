package com.api.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.api.api.DTO.LoginRequest;
import com.api.api.auth.JWTUtil;
import com.api.api.model.User;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

/*
 * Definimos endpoints para el login y para la renovación de tokens (si se decide implementar).
 * En el login, se recibirán las credenciales del usuario (correo y contraseña), se validarán usando el servicio de usuario y
 * , en caso de éxito, se generará el token de acceso (y opcionalmente un refresh token).
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    //ENDPOINT PARA EL LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody @Valid LoginRequest loginRequest) {
        //Creamos el token de autenticación a partir de las credenciales del usuario
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        //Si la autenticación es correcta, generamos el token de acceso
        String jwt = jwtUtil.generateToken(loginRequest.getEmail());

        //Se puede retorna un objeto JSON que devuelva el token y datos adicionales si son necesarios
        return ResponseEntity.ok(new JWTAuthResponse(jwt));

    }

    
}

//Clase para la respuesta que mandamos en la respuesta del endpoitn del login
@Getter
@Setter
class JWTAuthResponse {

    private String token;

    public JWTAuthResponse(String token) {
        this.token = token;
    }
}
