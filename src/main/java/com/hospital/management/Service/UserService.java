package com.hospital.management.Service;

import com.hospital.management.DTO.LoginRequest;
import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Entity.BloodType;
import com.hospital.management.Entity.Patient;
import com.hospital.management.Entity.Role;
import com.hospital.management.Entity.User;
import com.hospital.management.Repository.PatientRepository;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    // =========================
    // 🔥 REGISTER
    // =========================
    @Transactional
    public UserResponse register(RegisterRequest request) {

        validateUser(request.getUsername(), request.getEmail(), request.getTckn());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // BCrypt sonra
        user.setEmail(request.getEmail());
        user.setTckn(request.getTckn());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(Role.PATIENT);

        User savedUser = userRepository.save(user);

        // =========================
        // 🔥 PATIENT OLUŞTURMA
        // =========================
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setBirthDate(request.getBirthDate());
        patient.setBloodType(request.getBloodType());

        patientRepository.save(patient);

        return mapToResponse(savedUser);

    }

    // =========================
    // 🔥 LOGIN
    // =========================
    public UserResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Şifre hatalı");
        }

        return mapToResponse(user);
    }

    // =========================
    // 🔥 UPDATE
    // =========================
    @Transactional
    public UserResponse updateUser(Long id, RegisterRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        checkEmailUpdate(user, request.getEmail());
        checkUsernameUpdate(user, request.getUsername());
        checkTcknUpdate(user, request.getTckn());

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTckn(request.getTckn());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword());
        }

        return mapToResponse(userRepository.save(user));
    }

    // =========================
    // 🔥 DELETE
    // =========================
    @Transactional
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        userRepository.delete(user);
    }

    // =========================
    // 🔥 GET ALL
    // =========================
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // 🔥 VALIDATION
    // =========================
    private void validateUser(String username, String email, String tckn) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username kullanımda");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email kullanımda");
        }

        if (userRepository.existsByTckn(tckn)) {
            throw new RuntimeException("TCKN kullanımda");
        }
    }

    // =========================
    // UPDATE CHECKS
    // =========================
    private void checkEmailUpdate(User user, String newEmail) {
        if (newEmail == null || newEmail.equals(user.getEmail()))
            return;

        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email kullanımda");
        }
    }

    private void checkUsernameUpdate(User user, String newUsername) {
        if (newUsername == null || newUsername.equals(user.getUsername()))
            return;

        if (userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("Username kullanımda");
        }
    }

    private void checkTcknUpdate(User user, String newTckn) {
        if (newTckn == null || newTckn.equals(user.getTckn()))
            return;

        if (userRepository.existsByTckn(newTckn)) {
            throw new RuntimeException("TCKN kullanımda");
        }
    }

    // =========================
    // 🔥 MAPPER
    // =========================
    private UserResponse mapToResponse(User user) {

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);

        return response;
    }
}