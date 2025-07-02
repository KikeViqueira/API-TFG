package com.api.api.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.api.api.DTO.LoginRequest;
import com.api.api.auth.JWTUtil;
import com.api.api.model.User;
import com.api.api.service.UserService;

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

    @Autowired
    private UserService userService;

    //ENDPOINT PARA EL LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody @Valid LoginRequest loginRequest) {
        //Creamos el token de autenticación a partir de las credenciales del usuario
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        //Obtenemos usando la función del service de user el id del usuario para devolverlo en la llamada al endpoint y así poder guardarlo en el front
        User user = userService.getUserByEmail(loginRequest.getEmail());

        //Si la autenticación es correcta, generamos el token de acceso y el de refresco en base al id del usuario
        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        //Se puede retorna obejeto json que contiene los tokens
        return ResponseEntity.ok(new JWTAuthResponse.JWTAuthLogin(accessToken, refreshToken, user.getId()));
    }

    //Endpoint para la renovación del token en base al refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody HashMap<String, String> request) {
        //Extraemos el token de refresco de la petición
        String refreshToken = request.get("refreshToken");

        if (Objects.isNull(refreshToken) || !jwtUtil.validateToken(refreshToken)){
            //Devolvemos un error al usuario diciendo que no tiene acceso a la petición
            Map<String, String> error = new HashMap<>();
            error.put("error", "Refresh token caducado o inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        //Extraemos el id del token de refresco en caso contrario
        Long idUser = jwtUtil.getIdFromJWT(refreshToken);
        //generamos un nuevo access token y un nuevo refresh token (El token de refresco aunque no haya caducado se renueva por temas de seguridad)
        String newAccessToken = jwtUtil.generateToken(idUser);
        String newRefreshToken = jwtUtil.generateRefreshToken(idUser);
        
        return ResponseEntity.ok(new JWTAuthResponse.JWTAuthRefresh(newAccessToken, newRefreshToken));
    }
}

//Clases para las respuestas que mandamos en la respuesta del endpoitn del login y del refresh
class JWTAuthResponse {
    @Getter @Setter
    public static class JWTAuthLogin {
        private String accessToken;

        private String refreshToken;
    
        private Long userId;
    
        public JWTAuthLogin(String accessToken, String refreshToken, Long userId) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userId = userId;
        }
    }

    @Getter @Setter
    public static class JWTAuthRefresh {
        private String accessToken;

        private String refreshToken;
    
        public JWTAuthRefresh(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
