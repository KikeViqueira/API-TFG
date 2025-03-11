package com.api.api.auth;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/*
 * Filtro para validar el token estático en endpoints de /mock-fitbit.
 * Si la ruta comienza por /mock-fitbit, se verifica que la cabecera Authorization
 * contenga el token esperado ("fake_fitbit_token"). Si no es así, se responde con UNAUTHORIZED.
 * 
 * La anotación @Component le indica a Spring que esa clase debe ser gestionada como un bean,
 *  es decir, que Spring se encargue de crearla, instanciarla y mantenerla en su contenedor
 *  de dependencias. Así, se puede inyectar fácilmente en otras clases (por ejemplo, con @Autowired).
 * 
 * En el caso del filtro, al marcarlo con @Component, Spring lo reconoce y lo registra automáticamente
 * para que se integre en la cadena de filtros de seguridad configurada. Esto permite que,
 * cuando se procesa una petición, el filtro se ejecute sin tener que instanciarlo manualmente
 */
@Component
public class FitbitTokenFilter extends OncePerRequestFilter {

    //Definimos el token estático que el user ha de proporcionar a los endpoints de /fitbit
    private static final String STATIC_TOKEN = "ACCESS_TOKEN_VALUE";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{

        //Verificamos que las peticiones sean a la url que recogen los endpoints relacionados con Fitbit
        String servletPath = request.getServletPath();

         // Si la ruta es /api/fitbitAuth, saltar la validación del token
        if (servletPath.startsWith("/api/fitbitAuth")) {
            filterChain.doFilter(request, response);
            return;
        }


        if (servletPath.startsWith("/api/fitbit")){
            //recuperamos el token de la cabecera
            String token = request.getHeader("Authorization");
            //Si no existe o no es igual al token estático se rechhaza la petición
            if (token == null || !token.equals(STATIC_TOKEN)){
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }

        /*
         * Si no establecemos una validación en el contexto de seguridad, Spring Security no sabrá que el user está autenticado
         * y dara 403 aunque el token sea correcto. Por eso es indispensable establecer una autenticación en el contexto.
         */
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken("fitbitUser", null, List.of(new SimpleGrantedAuthority("ROLE_FITBIT")));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        //Continuamos con la cadena de filtros
        filterChain.doFilter(request, response);
    }
    
}
