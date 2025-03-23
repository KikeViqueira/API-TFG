package com.api.api.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
 * Encargada de generar y validar los tokens. En esta clase se definen métodos para firmar el token, 
 * extraer datos (como el email o el id del usuario), y verificar su validez y expiración. 
 * Es fundamental utilizar un algoritmo seguro (como HS512) y mantener la clave secreta protegida,
 * preferiblemente en variables de entorno o en un vault
 */

 @Component //le dice a Spring que debe gestionar la clase como un bean, permitiendo su inyección automática en otras partes del sistema.
public class JWTUtil {

    @Value("${jwt.secret}") // Inyecta el valor de la propiedad jwt.secret del archivo application.properties
    private String secret;

    @Value("${jwt.expiration}") //En milisegundos
    private Long expiration;

    @Value("${jwt.refreshExpiration}") //En milisegundos
    private Long refreshExpiration; //Tiempo de expiración del token de refresco

    //Generamos el token con la información del user
    public String generateToken(String email){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)),SignatureAlgorithm.HS512)//Nos aseguramos de que la clave cumpla con los requisitos de tamaño
                .compact();
    }

    //Definimos el nuevo método para genera el token de refresco
    public String generateRefreshToken(String email){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)),SignatureAlgorithm.HS512)//Nos aseguramos de que la clave cumpla con los requisitos de tamaño
                .compact();
    }

    //Función para extraer la info del token
    public String getUsernameFromJWT(String token){
        Claims claims = Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    //Función para comprobar si el token es válido y no ha expirado
    public boolean validateToken(String token){
        try {
            Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }    
}
