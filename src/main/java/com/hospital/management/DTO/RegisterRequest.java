package com.hospital.management.DTO;

import com.hospital.management.Entity.BloodType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {


    @Size(min = 3, max = 30)
    private String username;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @Email
    @NotBlank
    private String email;

    @Pattern(regexp = "^[0-9]{11}$", message = "TCKN 11 haneli olmalıdır")
    private String tckn;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Past
    private LocalDate birthDate;

    @NotNull
    private BloodType bloodType;
}