package com.hospital.management.Controller;

import com.hospital.management.Config.CustomUserDetails;
import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    // --- KENDİ PROFİL İŞLEMLERİ (ME) ---

    // Kullanıcılar kendi profillerini görebilir.
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getByTckn(userDetails.getUsername());
    }

    // Kullanıcılar kendi profillerini güncelleyebilir.
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RegisterRequest request) {
        return userService.updateByTckn(userDetails.getUsername(), request);
    }

    // Kullanıcılar kendi hesaplarını silebilir.
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public void deleteMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteById(userDetails.getUser().getId());
    }

    // --- YÖNETİMSEL VE ÖZEL ERİŞİM İŞLEMLERİ ---

    // Admin tüm kullanıcıları görebilir.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    // Admin ve Doktorlar, ID'si verilen kullanıcıyı görebilir. Kendi bilgilerini de
    // görebilirler.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    // Admin herhangi bir kullanıcıyı ID üzerinden silebilir.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }
}