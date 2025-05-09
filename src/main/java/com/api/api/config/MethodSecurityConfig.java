package com.api.api.config;

import com.api.api.security.OwnershipEvaluator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)  // Activa @PreAuthorize y @PostAuthorize
public class MethodSecurityConfig {

    private final OwnershipEvaluator ownershipEvaluator;

    public MethodSecurityConfig(OwnershipEvaluator ownershipEvaluator) {
        this.ownershipEvaluator = ownershipEvaluator;
    }

    /*
     * Definimos el bean que Spring Security usará para resolver expresiones SpEL
     * como hasPermission(...). Aquí inyectamos nuestro evaluador personalizado.
     */
    @Bean
    public MethodSecurityExpressionHandler expressionHandler() {
        DefaultMethodSecurityExpressionHandler handler =
            new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(ownershipEvaluator);
        return handler;
    }
}
