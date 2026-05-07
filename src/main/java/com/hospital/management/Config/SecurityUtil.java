package com.hospital.management.Config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

public class SecurityUtil {

    // USERNAME AL
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Kullanıcı doğrulanmamış");
        }

        return auth.getName();
    }

   
    public static String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN");
    }

    // ADMIN KONTROLÜ
    public static boolean isAdmin() {
        String role = getCurrentRole();
        return "ROLE_ADMIN".equals(role) || "ADMIN".equals(role);
    }

    public static boolean isOwner(String entityUsername) {
        return entityUsername != null &&
                entityUsername.equals(getCurrentUsername());
    }
}