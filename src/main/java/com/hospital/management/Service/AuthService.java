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

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
        private final UserRepository userRepository;
        private final PatientRepository patientRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final EntityManager entityManager; // EntityManager eklendi

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

        @Transactional(noRollbackFor = BadRequestException.class)
        public AuthResponse login(LoginRequest request) {
                User user = null;
                String loginInput = request.getTckn(); // Bu username veya TCKN olabilir

                // 1. Önce username olarak ara (Doktor ve Admin için)
                if (userRepository.findByUsername(loginInput).isPresent()) {
                        user = userRepository.findByUsername(loginInput).get();
                        System.out.println("✅ Username ile giriş: " + loginInput);
                }
                // 2. Bulamazsa TCKN olarak ara (Hastalar için)
                else if (userRepository.findByTckn(loginInput).isPresent()) {
                        user = userRepository.findByTckn(loginInput).get();
                        System.out.println("✅ TCKN ile giriş: " + loginInput);
                }

                if (user == null) {
                        throw new BadRequestException("TC Kimlik No / Kullanıcı adı veya şifre hatalı");
                }

                entityManager.refresh(user);

                // Hesap kilitli mi kontrol et
                if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
                        if (user.getLockTime() != null && user.getLockTime().plusMinutes(LOCK_TIME_DURATION)
                                        .isBefore(LocalDateTime.now())) {
                                user.setAccountNonLocked(true);
                                user.setFailedAttempt(0);
                                user.setLockTime(null);
                                userRepository.save(user);
                        } else {
                                long remainingMinutes = 0;
                                if (user.getLockTime() != null) {
                                        remainingMinutes = LOCK_TIME_DURATION -
                                                        java.time.Duration
                                                                        .between(user.getLockTime(),
                                                                                        LocalDateTime.now())
                                                                        .toMinutes();
                                        if (remainingMinutes < 0)
                                                remainingMinutes = 0;
                                }
                                throw new BadRequestException("Hesabınız kilitlenmiştir. " + remainingMinutes
                                                + " dakika sonra tekrar deneyin.");
                        }
                }

                // Şifre kontrolü
                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        processFailedAttempt(user);
                        entityManager.refresh(user);
                        int newFailedAttempt = user.getFailedAttempt() != null ? user.getFailedAttempt() : 0;
                        int remainingAttempts = MAX_FAILED_ATTEMPTS - newFailedAttempt;
                        throw new BadRequestException("TC Kimlik No / Kullanıcı adı veya şifre hatalı. " +
                                        remainingAttempts + " hakkınız kaldı.");
                }

                // Başarılı giriş
                user.setFailedAttempt(0);
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                userRepository.save(user);
                userRepository.flush();

                String token = jwtUtil.generateToken(user.getId(), user.getTckn(), user.getRole().name());
                return new AuthResponse(token, mapToUserResponse(user));
        }

        private void processFailedAttempt(User user) {
                int currentAttempts = user.getFailedAttempt() != null ? user.getFailedAttempt() : 0;
                int newAttempts = currentAttempts + 1;

                if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                        // Hesabı kilitle
                        user.setAccountNonLocked(false);
                        user.setLockTime(LocalDateTime.now());
                        user.setFailedAttempt(newAttempts);
                        userRepository.save(user);

                } else {

                        user.setFailedAttempt(newAttempts);
                        userRepository.save(user);
                }

                // Değişikliklerin hemen veritabanına yazılmasını sağla
                userRepository.flush();
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