package com.hospital.management.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String tckn;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String bloodGroup;
    private Integer age;
}