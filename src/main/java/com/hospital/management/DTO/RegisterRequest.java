package com.hospital.management.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

import com.hospital.management.Entity.BloodType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String username;
    private String password;
    private String email;
    private String tckn;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private BloodType bloodType;
}