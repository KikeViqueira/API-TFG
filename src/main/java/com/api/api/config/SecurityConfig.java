package com.api.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desactiva CSRF (opcional si no usas formularios web)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/user/register").permitAll() // Permitir registro sin autenticación
                .anyRequest().authenticated()
            )
            .httpBasic(); // Mantiene autenticación básica para otras rutas

        return http.build();
    }
}
