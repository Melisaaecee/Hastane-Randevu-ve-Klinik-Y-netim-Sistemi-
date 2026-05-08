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

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PatientRepository patientRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                validateUser(request.getUsername(), request.getEmail(), request.getTckn());

                User user = new User();
                user.setUsername(request.getUsername());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setEmail(request.getEmail());
                user.setTckn(request.getTckn());
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setRole(Role.PATIENT);

                User savedUser = userRepository.save(user);

                Patient patient = new Patient();
                patient.setUser(savedUser);
                patient.setBirthDate(request.getBirthDate());
                patient.setBloodType(request.getBloodType());
                patientRepository.save(patient);

                String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername(),
                                savedUser.getRole().name());
                return new AuthResponse(token, mapToUserResponse(savedUser));
        }

        public AuthResponse login(LoginRequest request) {
                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new BadRequestException("Kullanıcı adı veya şifre hatalı"));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new BadRequestException("Kullanıcı adı veya şifre hatalı");
                }

                String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
                return new AuthResponse(token, mapToUserResponse(user));
        }

        private void validateUser(String username, String email, String tckn) {
                if (userRepository.existsByUsername(username))
                        throw new BadRequestException("Username kullanımda");
                if (userRepository.existsByEmail(email))
                        throw new BadRequestException("Email kullanımda");
                if (userRepository.existsByTckn(tckn))
                        throw new BadRequestException("TCKN kullanımda");
        }

        private UserResponse mapToUserResponse(User user) {
                return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        }
}