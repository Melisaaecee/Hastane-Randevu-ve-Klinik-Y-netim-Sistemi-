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
    public UserResponse updateByTckn(String tckn, RegisterRequest request) {
        User user = getUserByTckn(tckn);
        if (!SecurityUtil.isOwner(user.getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Sadece kendi profilinizi güncelleyebilirsiniz.");
        }
        validateUpdate(user, request);
        applyUpdates(user, request);
        return mapToResponse(userRepository.save(user));
    }

    public UserResponse getByTckn(String tckn) {
        User user = getUserByTckn(tckn);
        return mapToResponse(user);
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

    private User getUserByTckn(String tckn) {
        return userRepository.findByTckn(tckn)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı (TCKN: " + tckn + ")"));
    }

    private void validateUpdate(User user, RegisterRequest request) {
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email kullanımda");
        if (!user.getTckn().equals(request.getTckn()) && userRepository.existsByTckn(request.getTckn()))
            throw new BadRequestException("Bu TC Kimlik Numarası zaten kayıtlı.");
    }

    private void applyUpdates(User user, RegisterRequest request) {
        user.setTckn(request.getTckn());
        user.setUsername(request.getTckn()); // Username'i TCKN ile senkron tutuyoruz
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getTckn(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name());
    }
}