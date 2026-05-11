package com.hospital.management.Config;

import com.hospital.management.Entity.User;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Önce TCKN ile ara (hastalar için)
        // Sonra username ile ara (doktor ve admin için)
        User user = userRepository.findByTckn(username)
                .orElseGet(() -> userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username)));

        return new CustomUserDetails(user);
    }
}