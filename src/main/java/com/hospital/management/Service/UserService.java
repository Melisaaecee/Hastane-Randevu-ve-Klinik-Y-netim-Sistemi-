package com.hospital.management.Service;

import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Entity.User;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // =========================
    // GET ALL (ADMIN)
    // =========================
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // GET BY ID (ADMIN)
    // =========================
    public UserResponse getById(Long id) {
        return mapToResponse(getUserById(id));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    // =========================
    // UPDATE BY ID (ADMIN)
    // =========================
    @Transactional
    public UserResponse updateUser(Long id, RegisterRequest request) {

        User user = getUserById(id);

        validateUpdate(user, request);

        applyUpdates(user, request);

        return mapToResponse(userRepository.save(user));
    }

    // =========================
    // DELETE BY ID (ADMIN)
    // =========================
    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(getUserById(id));
    }

    // =========================
    // GET BY USERNAME (USER /me)
    // =========================
    public UserResponse getByUsername(String username) {
        return mapToResponse(getUserByUsername(username));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    // =========================
    // UPDATE BY USERNAME (/me)
    // =========================
    @Transactional
    public UserResponse updateByUsername(String username, RegisterRequest request) {

        User user = getUserByUsername(username);

        validateUpdate(user, request);

        applyUpdates(user, request);

        return mapToResponse(userRepository.save(user));
    }

    // =========================
    // DELETE BY USERNAME (/me)
    // =========================
    @Transactional
    public void deleteByUsername(String username) {
        userRepository.delete(getUserByUsername(username));
    }

    // =========================
    // VALIDATION
    // =========================
    private void validateUpdate(User user, RegisterRequest request) {

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email kullanımda");
        }

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username kullanımda");
        }

        if (!user.getTckn().equals(request.getTckn())
                && userRepository.existsByTckn(request.getTckn())) {
            throw new RuntimeException("TCKN kullanımda");
        }
    }

    // =========================
    // APPLY UPDATE
    // =========================
    private void applyUpdates(User user, RegisterRequest request) {

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTckn(request.getTckn());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword());
        }
    }

    // =========================
    // MAPPER
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