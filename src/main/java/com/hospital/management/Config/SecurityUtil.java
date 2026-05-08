package com.hospital.management.Config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    /**
     * Güvenlik bağlamındaki (Context) principal nesnesini CustomUserDetails tipinde
     * döner.
     */
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        return null;
    }

    /**
     * Mevcut giriş yapmış kullanıcının kullanıcı adını döner.
     */
    public static String getCurrentUsername() {
        CustomUserDetails details = getCurrentUserDetails();
        return (details != null) ? details.getUsername() : null;
    }

    /**
     * Mevcut kullanıcının veritabanı ID'sini döner.
     * getUser() üzerinden erişim sağlanır.
     */
    public static Long getCurrentUserId() {
        CustomUserDetails details = getCurrentUserDetails();
        return (details != null && details.getUser() != null) ? details.getUser().getId() : null;
    }

    /**
     * Mevcut kullanıcının TCKN bilgisini döner.
     * CustomUserDetails içindeki user nesnesi üzerinden erişir.
     */
    public static String getCurrentUserTckn() {
        CustomUserDetails details = getCurrentUserDetails();
        // getUser() üzerinden tckn alanına erişiyoruz
        return (details != null && details.getUser() != null) ? details.getUser().getTckn() : null;
    }

    /**
     * Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol eder.
     */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return false;

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Verilen userId, şu an login olan kullanıcının ID'sine eşit mi?
     */
    public static boolean isOwner(Long userId) {
        Long currentId = getCurrentUserId();
        return currentId != null && currentId.equals(userId);
    }
}