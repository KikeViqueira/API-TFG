package com.api.api.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.api.api.auth.CustomUserDetails;

import java.io.Serializable;
import java.util.Objects;

//Este bean implementa la interfaz oficial de Spring Security y centraliza la lógica de comparación
@Component
public class OwnershipEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication auth, Object targetId, Object permission) {
        //El "owner" es el nombre que le damos al permiso pero en este caso no lo necesitamos usar para nada
        if (Objects.isNull(auth) || !auth.isAuthenticated() || Objects.isNull(targetId)) {
            return false;
        }

        // Asegurarse de que el principal es CustomUserDetails
        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return false;
        }
        // Castea tu principal a CustomUserDetails, que expone getId()
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        // El targetId viene del @PreAuthorize: es el id del usuario que viene insertado en la url como @PathVariable
        Long resourceOwnerId;
        try {
            resourceOwnerId = Long.valueOf(targetId.toString());
        } catch (NumberFormatException e) {
            return false;
        }

        // Compara el ID autenticado con el ID del recurso, no usamos el permission que se ha tenido que pasar desde los controllers en este caso "owner"
        return Objects.equals(user.getId(), resourceOwnerId);
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId,
                                 String targetType, Object permission) {
        // No lo usamos
        return false;
    }
}
