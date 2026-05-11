package com.hospital.management.Config;

import com.hospital.management.Entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Username olarak TCKN döndür (Spring Security için)
        return user.getTckn();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Veritabanındaki accountNonLocked değerini kontrol et
        if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
            // Kilit süresi dolmuş mu kontrol et
            if (user.getLockTime() != null &&
                    user.getLockTime().plusMinutes(15).isBefore(LocalDateTime.now())) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }

    public long getRemainingLockMinutes() {
        if (Boolean.TRUE.equals(user.getAccountNonLocked()) || user.getLockTime() == null) {
            return 0;
        }
        long remaining = 15 - java.time.Duration.between(user.getLockTime(), LocalDateTime.now()).toMinutes();
        return remaining > 0 ? remaining : 0;
    }
}