package com.hospital.management.Controller;

import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ADMIN - TÜM KULLANICILAR
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    // ADMIN - ID ile kullanıcı getir
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    // USER - kendi profilini gör
    @GetMapping("/me")
    public UserResponse getMyProfile(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return userService.getByUsername(user.getUsername());
    }

    // USER - kendi hesabını güncelle
    @PutMapping("/me")
    public UserResponse updateMyProfile(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @RequestBody RegisterRequest request) {

        return userService.updateByUsername(user.getUsername(), request);
    }

    // USER - kendi hesabını sil
    @DeleteMapping("/me")
    public void deleteMyAccount(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

        userService.deleteByUsername(user.getUsername());
    }

    // ADMIN - kullanıcı sil
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // ADMIN - kullanıcı güncelle
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequest request) {

        return userService.updateUser(id, request);
    }
}