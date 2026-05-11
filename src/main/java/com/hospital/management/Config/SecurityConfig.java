package com.hospital.management.Config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin())
                                                .xssProtection(xss -> xss.headerValue(
                                                                org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                                                .contentSecurityPolicy(cps -> cps.policyDirectives(
                                                                "script-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com; style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com")))
                                .authorizeHttpRequests(auth -> auth
                                                // 1. STATİK DOSYALAR (Herkes erişebilmeli)
                                                .requestMatchers(
                                                                "/",
                                                                "/index.html",
                                                                "/login.html",
                                                                "/register.html",
                                                                "/reset-password.html",
                                                                "/patient.html",
                                                                "/doctor-dashboard.html",
                                                                "/admin.html",
                                                                "/appointment.html",
                                                                "/style.css",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/javascript/**",
                                                                "/images/**",
                                                                "/favicon.ico",
                                                                "/static/**",
                                                                "/resources/**",
                                                                "/public/**")
                                                .permitAll()

                                                // 2. GENEL API ENDPOINTLERİ
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/api/cities/**", "/api/districts/**").permitAll()
                                                .requestMatchers("/api/doctors/**").permitAll()
                                                .requestMatchers("/api/slots/**").permitAll()

                                                // ÖNEMLİ: Railway sağlık kontrolleri (health checks) için gerekebilir
                                                .requestMatchers("/actuator/**").permitAll()

                                                // 3. ROL BAZLI ERİŞİM
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/users/**").hasRole("ADMIN")

                                                // 4. DİĞER HER ŞEY
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter()
                                                                        .write("{\"message\": \"Kimlik dogrulama basarisiz veya yetkiniz yok.\"}");
                                                }))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Railway ve Localhost için CORS ayarları
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:8080",
                                "http://127.0.0.1:5500", // Live Server için
                                "https://medsoft.up.railway.app", // Kendi Railway adresin
                                "https://medsoft.com" // Eklediğin özel domain
                ));

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(
                                Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L); // Tarayıcı önbelleği için

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @jakarta.annotation.PostConstruct
        public void enableAuthOnAsyncThreads() {
                SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        }

        @Configuration
        public class WebConfig implements WebMvcConfigurer {
                @Override
                public void addResourceHandlers(ResourceHandlerRegistry registry) {
                        registry.addResourceHandler("/javascript/**")
                                        .addResourceLocations("classpath:/static/javascript/");
                }
        }
}