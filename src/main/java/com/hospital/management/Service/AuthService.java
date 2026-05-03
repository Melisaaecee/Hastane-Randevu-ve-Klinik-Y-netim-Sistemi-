package com.hospital.management.Service;

import com.hospital.management.DTO.LoginRequest;
import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Entity.Patient;
import com.hospital.management.Entity.Role;
import com.hospital.management.Entity.User;
import com.hospital.management.Repository.PatientRepository;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    
    // REGISTER
    @Transactional
    public UserResponse register(RegisterRequest request) {

        validateUser(request.getUsername(), request.getEmail(), request.getTckn());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
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

        return mapToResponse(savedUser);
    }

   
    // LOGIN
    public UserResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Şifre hatalı");
        }

        return mapToResponse(user);
    }


    // VALIDATION
    private void validateUser(String username, String email, String tckn) {

        if (userRepository.existsByUsername(username))
            throw new RuntimeException("Username kullanımda");

        if (userRepository.existsByEmail(email))
            throw new RuntimeException("Email kullanımda");

        if (userRepository.existsByTckn(tckn))
            throw new RuntimeException("TCKN kullanımda");
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }
}