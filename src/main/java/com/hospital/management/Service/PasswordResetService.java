package com.hospital.management.Service;

import com.hospital.management.Entity.PasswordResetToken;
import com.hospital.management.Entity.User;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.PasswordResetTokenRepository;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder; // SecurityConfig'deki BCrypt'i otomatik alır

    @Transactional
    public void createResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Bu e-posta ile kayıtlı kullanıcı bulunamadı."));

        // Güvenlik: Kullanıcının bekleyen eski talepleri varsa temizle
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // 15 dakika limit

        tokenRepository.save(resetToken);

        mailService.sendPasswordResetMail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Geçersiz veya kullanılmış şifre sıfırlama linki!"));

        // Süre Kontrolü
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Şifre sıfırlama linkinin süresi dolmuş, lütfen tekrar talep edin.");
        }

        User user = resetToken.getUser();
        
        // 🔥 KRİTİK: SecurityConfig'deki BCrypt ile yeni şifreyi hash'liyoruz
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Güvenlik: Kullanılan token'ı imha et (Tek kullanımlık link)
        tokenRepository.delete(resetToken);
    }
}
