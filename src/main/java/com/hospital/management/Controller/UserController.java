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

    // USER - ME

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public UserResponse getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getByUsername(userDetails.getUsername());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public UserResponse updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RegisterRequest request) {
        return userService.updateByUsername(userDetails.getUsername(), request);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public void deleteMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteByUsername(userDetails.getUsername());
    }

    // ADMIN
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
    public UserResponse updateUser(@PathVariable Long id,
            @RequestBody RegisterRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}