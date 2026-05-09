package com.hospital.management.Controller;

import com.hospital.management.DTO.*;
import com.hospital.management.Entity.User;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.UserRepository;
import com.hospital.management.Service.AuthService;
import com.hospital.management.Service.MailService;
import com.hospital.management.Service.PasswordResetService;
import com.hospital.management.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final MailService mailService;
    private final UserService userService;
    private final UserRepository userRepository;

    // --- KAYIT VE GİRİŞ İŞLEMLERİ ---

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // --- ŞİFRE SIFIRLAMA İŞLEMLERİ ---

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.createResetToken(request.getEmail());
        return ResponseEntity.ok("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.");
    }

    // --- PANEL İÇİNDEN ŞİFRE GÜNCELLEME 
    @PostMapping("/reset-password-logged-in")
    public ResponseEntity<?> resetPasswordLoggedIn(@RequestBody Map<String, String> request) {
        try {
           
            userService.updatePassword(
                    request.get("tckn"),
                    request.get("currentPassword"),
                    request.get("newPassword"));

            return ResponseEntity.ok(Map.of("message", "Şifreniz başarıyla güncellendi"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    // E-POSTA DOĞRULAMA KODU GÖNDERME
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String tckn = request.get("tckn"); // Frontend'den TCKN gelmeli

        // MailService artık 2 parametre bekliyor: to ve tckn
        mailService.sendVerificationCode(email, tckn);
        return ResponseEntity.ok(Map.of("message", "Kod gönderildi"));
    }

    // E-POSTA GÜNCELLEME ONAYLAMA 
    @PostMapping("/verify-email-update")
    public ResponseEntity<?> verifyEmailUpdate(@RequestBody Map<String, String> request) {
        String incomingCode = request.get("code");
        String newEmail = request.get("newEmail");
        String tckn = request.get("tckn");

        User user = userRepository.findByTckn(tckn)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));

        // DB'deki kod ile gelen kodu karşılaştır
        if (user.getVerificationCode() != null && user.getVerificationCode().equals(incomingCode)) {

            userService.updateEmailByTckn(tckn, newEmail);
            user.setVerificationCode(null);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "E-posta başarıyla güncellendi"));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Geçersiz veya hatalı doğrulama kodu!"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Şifreniz başarıyla güncellendi.");
    }
}