package com.api.api.auth;

import java.io.IOException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.api.api.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 *  Encargado de interceptar las peticiones entrantes, extraer el token de la cabecera Authorization (por convención, con el prefijo "Bearer "),
 *  y validar el token utilizando JWTUtil. Si el token es válido, el filtro creará un objeto de autenticación (por ejemplo,
 *  un UsernamePasswordAuthenticationToken) y lo establecerá en el contexto de Spring Security, de modo que los endpoints protegidos
 *  puedan acceder a la información del usuario autenticado.
 */

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String servletPath = request.getServletPath();

        // Omitir el filtro para rutas públicas de auth y para POST en /api/users
        if (servletPath.startsWith("/api/auth/") || (servletPath.equals("/api/users") && request.getMethod().equalsIgnoreCase("POST"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // Si la solicitud es para /api/fitbit, se omite la validación JWT
        if (request.getServletPath().startsWith("/api/fitbit")) {
            filterChain.doFilter(request, response);
            return;
        }

        //Extraemos el token de la cabecera Authorization
        String header = request.getHeader("Authorization");
        String token = null;
        Long idUser = null;

        //Comprobamos si la cabecera contiene el token y si empieza por "Bearer "
        if (Objects.nonNull(header) && header.startsWith("Bearer ")){
            token = header.substring(7); //Extraemos el token sin el prefijo "Bearer "
            if (this.jwtUtil.validateToken(token)){

            idUser = this.jwtUtil.getIdFromJWT(token); //Extraemos la info del cuerpo del token

            var userDetails = customUserDetailsService.loadUserById(idUser);

            UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
              );
            authToken.setDetails(new WebAuthenticationDetailsSource()
                                   .buildDetails(request));

            /*
             *  REEMPLAZAMOS LA AUTH PREVIA SIEMPRE QUE TENEMOS UN JWT VALIDO POR SI SE ANTES
             *  TENÍAMOS EN SU LUGAR EL DE FITBIT, Y QUE ASI FUNCIONE CORRECTAMENTE EL EVALUADOR DE PERMISOS
             *  YA QUE PRINCIPAL AUTH NO SERÁ UN STRING Y MO DARÁ PROBLEMAS DE CASTEO
            */
            SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
