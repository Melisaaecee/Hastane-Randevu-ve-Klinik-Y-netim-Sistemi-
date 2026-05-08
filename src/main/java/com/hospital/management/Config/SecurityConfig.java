package com.hospital.management.Config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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

@Configuration
@EnableMethodSecurity // Controller'lardaki @PreAuthorize notasyonlarını aktif eder
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Stateless API olduğu için kapatıldı
                .cors(cors -> cors.configure(http)) // Frontend bağlantısı için CORS aktif
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT
                                                                                                        // kullandığımız
                                                                                                        // için session
                                                                                                        // tutmuyoruz
                .authorizeHttpRequests(auth -> auth

                        // 1. TAMAMEN AÇIK ENDPOINTLER (Giriş yapmadan erişilebilir)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/cities/**", "/api/districts/**").permitAll() // Şehir ve ilçe listesi
                                                                                            // randevu öncesi lazım

                        // 2. SADECE ADMIN ERİŞİMİ
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 3. DOKTOR VE ADMIN ERİŞİMİ
                        .requestMatchers("/api/slots/**").hasAnyRole("DOCTOR", "ADMIN")

                        // 4. DİĞER TÜM API'LER (Giriş zorunlu, detay yetkiler Controller'da)
                        // Hastaneler, Klinikler, Doktorlar ve Randevular login olan herkes tarafından
                        // görülmeli
                        .requestMatchers(
                                "/api/hospitals/**",
                                "/api/clinics/**",
                                "/api/doctors/**",
                                "/api/patients/**",
                                "/api/appointments/**")
                        .authenticated()

                        // 5. Geri kalan her şey
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @PostConstruct
    public void enableAuthOnAsyncThreads() {
        // Asenkron işlemlerde güvenlik bağlamını korur
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}