package com.api.api.auth;

import com.api.api.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Método adicional para acceder al id del usuario
    public Long getId() {
        return user.getId();
    }
    
    // Método adicional para acceder al nombre del usuario
    public String getName() {
        return user.getName();
    }
    
    // Devuelve el email del usuario como username
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // Devuelve la contraseña encriptada
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Aquí se definen los roles o permisos del usuario. 
    // Suponiendo que el campo role en User es un String (por ejemplo, "USER" o "ADMIN"),
    // lo formateamos como "ROLE_USER" para seguir la convención de Spring Security.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
    }

    // Asumimos que la cuenta no ha expirado
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Asumimos que la cuenta no está bloqueada
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Asumimos que las credenciales no han expirado
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Asumimos que el usuario está habilitado
    @Override
    public boolean isEnabled() {
        return true;
    }
}

