package com.hospital.management.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableMethodSecurity // Controller'lardaki @PreAuthorize etiketlerinin çalışması için kritik!
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // Stateless API olduğu için kapalı
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 1. HERKESE AÇIK (Auth işlemleri)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. HASTANE & KLİNİK LİSTELEME (Burası Değişti!)
                        // Hastaların hastaneleri görmesi lazım. @PreAuthorize ile Controller'da POST/DELETE'i kısıtlayacağız.
                        .requestMatchers("/api/hospitals/**").hasAnyRole("ADMIN", "PATIENT", "DOCTOR")
                        .requestMatchers("/api/clinics/**").hasAnyRole("ADMIN", "PATIENT", "DOCTOR")
                        .requestMatchers("/api/doctors/**").hasAnyRole("ADMIN", "PATIENT", "DOCTOR")

                        // 3. ADMIN ÖZEL (Kritik yönetim işlemleri)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 4. DOCTOR + ADMIN (Randevu slotları)
                        .requestMatchers("/api/slots/**").hasAnyRole("DOCTOR", "ADMIN")

                        // 5. GENEL ERİŞİM (Randevu ve Lokasyon)
                        .requestMatchers(
                                "/api/appointments/**",
                                "/api/patients/**",
                                "/api/penalties/**",
                                "/api/districts/**",
                                "/api/cities/**"
                        ).hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                        .anyRequest().authenticated()
                )
                // JWT Filtresini ekle
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS AYARLARI (Frontend erişimi için)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000") // React/Vue portu
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @PostConstruct
    public void enableAuthOnAsyncThreads() {
        SecurityContextHolder.setStrategyName(
                SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}