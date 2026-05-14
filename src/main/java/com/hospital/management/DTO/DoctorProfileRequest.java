package com.hospital.management.DTO;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorProfileRequest {
    
    private String email;

    @Pattern(regexp = "^[0-9]{11}$", message = "TCKN 11 haneli olmalıdır")
    private String tckn;

    @Size(min = 3, max = 30)
    private String username;

}