package com.hospital.management.Controller;

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

    
    //  KULLANICI KENDİ PROFİLİ (/me)
    @GetMapping("/me")
    public UserResponse getMyProfile(@AuthenticationPrincipal String username) {
        return userService.getByUsername(username);
    }

    @PutMapping("/me")
    public UserResponse updateMyProfile(
            @AuthenticationPrincipal String username,
            @RequestBody RegisterRequest request) {
        return userService.updateByUsername(username, request);
    }

    @DeleteMapping("/me")
    public void deleteMyAccount(@AuthenticationPrincipal String username) {
        userService.deleteByUsername(username);
    }

   
    //  ADMIN PANELİ (Yalnızca Admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}