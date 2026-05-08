package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Entity.User;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public UserResponse getById(Long id) {

        if (!SecurityUtil.isOwner(id) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu kullanıcı bilgilerine erişim yetkiniz yok.");
        }
        return mapToResponse(getUserById(id));
    }

    @Transactional
    public UserResponse updateByUsername(String username, RegisterRequest request) {
        User user = getUserByUsername(username);
        if (!SecurityUtil.isOwner(user.getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Sadece kendi profilinizi güncelleyebilirsiniz.");
        }
        validateUpdate(user, request);
        applyUpdates(user, request);
        return mapToResponse(userRepository.save(user));
    }

    public UserResponse getByUsername(String username) {
        User user = getUserByUsername(username); // Mevcut private metodunu kullanıyoruz
        return mapToResponse(user); // Mevcut mapping metodunu kullanıyoruz
    }

    @Transactional
    public void deleteById(Long id) {
      
        if (!SecurityUtil.isOwner(id) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu hesabı silme yetkiniz yok.");
        }

        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Silinmek istenen kullanıcı bulunamadı (ID: " + id + ")");
        }

        userRepository.deleteById(id);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı (ID: " + id + ")"));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı: " + username));
    }

    private void validateUpdate(User user, RegisterRequest request) {
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email kullanımda");
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername()))
            throw new BadRequestException("Username kullanımda");
    }

    private void applyUpdates(User user, RegisterRequest request) {
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    private UserResponse mapToResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole().name());
        return res;
    }
}