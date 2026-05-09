package com.hospital.management.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "TC Kimlik no boş olamaz")
    private String tckn;

    @NotBlank(message = "Şifre boş olamaz")
    private String password;
}