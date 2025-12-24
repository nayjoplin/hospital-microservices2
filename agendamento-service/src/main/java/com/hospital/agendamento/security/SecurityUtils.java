package com.hospital.agendamento.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Jwt> currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }

    /**
     * Retorna um identificador estável do usuário autenticado.
     * Preferimos preferred_username (Keycloak), com fallback para sub.
     */
    public static String currentUsername() {
        return currentJwt()
            .map(jwt -> {
                String preferred = jwt.getClaimAsString("preferred_username");
                if (preferred != null && !preferred.isBlank()) {
                    return preferred;
                }
                return jwt.getSubject();
            })
            .orElse("anonymous");
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String expected = "ROLE_" + role;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (expected.equalsIgnoreCase(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Considera "usuário comum" quando tem USUARIO e não tem MEDICO/ADMIN.
     */
    public static boolean isPlainUsuario() {
        return hasRole("USUARIO") && !hasRole("MEDICO") && !hasRole("ADMIN");
    }
}
