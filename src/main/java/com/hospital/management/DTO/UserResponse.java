package com.hospital.management.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String token; // Token alanı eklendi: Frontend bu token'ı LocalStorage'da saklayacak.

}