package com.hospital.management.Config;

import com.hospital.management.Entity.Role;
import com.hospital.management.Entity.User;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Sistemde "admin" kullanıcı adıyla biri var mı bak
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); 
            admin.setEmail("admin@hastane.com");
            admin.setTckn("00000000000");
            admin.setFirstName("Sistem");
            admin.setLastName("Admini");
            admin.setRole(Role.ADMIN); 

            
            admin.setAccountNonLocked(true);
            admin.setFailedAttempt(0);
            userRepository.save(admin);

            System.out.println(">> [SİSTEM] Saf Admin kullanıcısı oluşturuldu. (Patient kaydı yoktur)");
        }
    }
}