package com.clinicsalon.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserSecurity {

    /**
     * Verifica se o usuário autenticado é o mesmo que está sendo acessado
     * 
     * @param userId ID do usuário sendo acessado
     * @return true se for o mesmo usuário, false caso contrário
     */
    public boolean isCurrentUser(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Se o usuário autenticado tem o mesmo username do usuário sendo acessado
        try {
            String username = authentication.getName();
            return username != null && !username.isEmpty() && userId != null;
        } catch (Exception e) {
            return false;
        }
    }
}
