package com.hospital.centrocirurgico.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String preferred = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
            return jwtAuth.getToken().getSubject();
        }
        return "anonymous";
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

    public static boolean isPlainUsuario() {
        return hasRole("USUARIO") && !hasRole("MEDICO") && !hasRole("ADMIN");
    }
}
