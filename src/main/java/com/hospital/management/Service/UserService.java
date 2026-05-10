package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Entity.Role; // Role importu eklendi
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
    public void updatePassword(String tckn, String currentPassword, String newPassword) {

        User user = userRepository.findByTckn(tckn)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));

        // 2. Mevcut şifre doğruluğunu kontrol et
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mevcut şifreniz hatalı!");
        }

        // 3. Yeni şifre eski şifreyle aynı mı kontrolü
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("Yeni şifre eskisinden farklı olmalıdır!");
        }

        // 4. Yeni şifreyi hashleyerek kaydet
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateByTckn(String tckn, RegisterRequest request) {
        User user = getUserByTckn(tckn);

        if (!SecurityUtil.isOwner(user.getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Yetkiniz yok.");
        }

        validateUpdate(user, request);

        if (user.getRole() != Role.PATIENT && request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        user.setTckn(request.getTckn());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void updateEmailByTckn(String tckn, String newEmail) {

        User user = getUserByTckn(tckn);

        // 2. Yeni email başka bir kullanıcıda var mı kontrol et
        if (userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("Bu e-posta adresi zaten başka bir kullanıcı tarafından kullanılıyor.");
        }
        user.setEmail(newEmail);
        userRepository.save(user);
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

    private UserResponse mapToResponse(User user) {
        String bloodGroup = "Belirtilmedi";
        Integer age = null;

        if (user.getPatient() != null) {
            if (user.getPatient().getBloodType() != null) {
                bloodGroup = user.getPatient().getBloodType().name();
            }
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