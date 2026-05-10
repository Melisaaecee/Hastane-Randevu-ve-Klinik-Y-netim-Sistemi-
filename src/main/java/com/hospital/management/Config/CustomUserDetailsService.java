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
    public UserDetails loadUserByUsername(String tckn) throws UsernameNotFoundException {
      
        User user = userRepository.findByTckn(tckn)
                .orElseThrow(() -> new UsernameNotFoundException("TCKN bulunamadı: " + tckn));

        return new CustomUserDetails(user);
    }
}