package com.hospital.management.Config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private static JwtUtil jwtUtil;

    public SecurityUtil(JwtUtil jwtUtil) {
        SecurityUtil.jwtUtil = jwtUtil;
    }

    public static Long getCurrentUserId() {
        String token = getToken();
        return jwtUtil.extractUserId(token);
    }

    public static String getCurrentUsername() {
        String token = getToken();
        return jwtUtil.extractUsername(token);
    }

    public static boolean isAdmin() {
        String role = jwtUtil.extractRole(getToken());
        return role.equals("ROLE_ADMIN");
    }

    public static boolean isOwner(Long userId) {
        return getCurrentUserId().equals(userId);
    }

    private static String getToken() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getCredentials()
                .toString();
    }
}