package com.hospital.management.Service;

import com.hospital.management.Config.JwtUtil;
import com.hospital.management.DTO.*;
import com.hospital.management.Entity.*;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
        private final UserRepository userRepository;
        private final PatientRepository patientRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;

        private static final int MAX_FAILED_ATTEMPTS = 3;
        private static final int LOCK_TIME_DURATION = 15;

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                validateUser(request.getEmail(), request.getTckn());

                User user = new User();
                user.setTckn(request.getTckn());
                user.setUsername(null);
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setEmail(request.getEmail());
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setRole(Role.PATIENT);
                user.setAccountNonLocked(true);
                user.setFailedAttempt(0);

                User savedUser = userRepository.save(user);

                Patient patient = new Patient();
                patient.setUser(savedUser);
                patient.setBirthDate(request.getBirthDate());
                patient.setBloodType(request.getBloodType());
                patientRepository.save(patient);

                String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getTckn(),
                                savedUser.getRole().name());
                return new AuthResponse(token, mapToUserResponse(savedUser));
        }

        @Transactional
        public AuthResponse login(LoginRequest request) {
                // TCKN veya Username ile bul
                User user = userRepository.findByTckn(request.getTckn())
                                .orElseGet(() -> userRepository.findByUsername(request.getTckn())
                                                .orElseThrow(() -> new BadRequestException("Giriş bilgileri hatalı.")));

                // Hesap kilitli mi kontrol et
                if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
                        if (user.getLockTime() != null && user.getLockTime().plusMinutes(LOCK_TIME_DURATION)
                                        .isBefore(LocalDateTime.now())) {
                                user.setAccountNonLocked(true);
                                user.setFailedAttempt(0);
                                user.setLockTime(null);
                                userRepository.save(user);
                        } else {
                                throw new BadRequestException("Hesabınız kilitli.");
                        }
                }

                // Şifre kontrolü
                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        processFailedAttempt(user);
                        throw new BadRequestException("TC Kimlik No veya şifre hatalı");
                }

                user.setFailedAttempt(0);
                userRepository.save(user);

                String token = jwtUtil.generateToken(user.getId(), user.getTckn(), user.getRole().name());
                return new AuthResponse(token, mapToUserResponse(user));
        }

        private void processFailedAttempt(User user) {
                int newAttempts = user.getFailedAttempt() + 1;
                user.setFailedAttempt(newAttempts);

                if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                        user.setAccountNonLocked(false);
                        user.setLockTime(LocalDateTime.now());
                }
                userRepository.save(user);
        }

        private void validateUser(String email, String tckn) {
                if (userRepository.existsByTckn(tckn))
                        throw new BadRequestException("Bu TC Kimlik Numarası ile zaten bir kayıt mevcut.");
                if (userRepository.existsByEmail(email))
                        throw new BadRequestException("Bu Email adresi zaten kullanımda.");
        }

        private UserResponse mapToUserResponse(User user) {
                String bloodGroup = "Belirtilmedi";
                Integer age = 0;

                if (user.getPatient() != null) {
                        bloodGroup = user.getPatient().getBloodType() != null ? user.getPatient().getBloodType().name()
                                        : "Belirtilmedi";
                        age = user.getPatient().getAge();
                }

                return new UserResponse(
                                user.getId(),
                                user.getTckn(),
                                user.getEmail(),
                                user.getUsername(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getRole().name(),
                                bloodGroup,
                                age);
        }

}