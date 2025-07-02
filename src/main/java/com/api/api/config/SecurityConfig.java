package com.api.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.api.api.auth.FitbitTokenFilter;
import com.api.api.auth.JWTAuthenticationFilter;


@Configuration
public class SecurityConfig {

    //Registramos el filtro de seguridad para los endpoints de relacionados con la WEB API de Fitbit
    @Autowired
    private FitbitTokenFilter fitbitTokenFilter;

    //Tenemos que registrar nuestro filtro de seguridad personalizado
    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // API REST sin estado
            .and()
            /*
             * si no llega un JWT válido, el contexto se queda sin Authentication y Spring responde
             * 401 Unauthorized en lugar de crear un principal anónimo que luego tu evaluador rechazaría con 403
             */
            .anonymous().disable() 
            .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Solo POST es público (registro) para que el nuevo usuario no necesite de un token JWT
            .requestMatchers("/api/auth/**").permitAll() // Otros endpoints públicos de auth
            .requestMatchers("/api/fitbitAuth/**").permitAll() // Permite el acceso a /api/fitbitAuth
            .anyRequest().authenticated() // El resto requiere autenticación
        );

        //Agregamos primero el filtro para /fitbit
        http.addFilterBefore(fitbitTokenFilter, UsernamePasswordAuthenticationFilter.class);
        //Agregamos nuestro filtro JWT para el resto de endpoints
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    //Exponemos el AuthenticationManager para poder utilizarlo en el controlador de autenticación
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //Tenemos que definir el encoder de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
