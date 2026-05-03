package com.hospital.management.Controller;

import com.hospital.management.DTO.LoginRequest;
import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //  REGISTER
    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    //  LOGIN
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    //  ADMIN → TÜM KULLANICILAR
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    //  UPDATE
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
            @RequestBody RegisterRequest request) {
        return userService.updateUser(id, request);
    }

    //  DELETE
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}