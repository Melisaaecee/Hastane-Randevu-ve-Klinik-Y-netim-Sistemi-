package com.hospital.management.Controller;

import com.hospital.management.DTO.AuthResponse;
import com.hospital.management.DTO.LoginRequest;
import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // --- KAYIT VE GİRİŞ İŞLEMLERİ ---
   
    // Kullanıcı kayıt işlemi. Yeni bir kullanıcı oluşturur ve JWT token döner.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

   
    // Kullanıcı giriş işlemi. Kullanıcı adı ve şifre doğrulaması yapar, başarılı ise JWT token döner.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}