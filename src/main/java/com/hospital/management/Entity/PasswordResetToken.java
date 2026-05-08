package com.hospital.management.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token; // UUID.randomUUID().toString() ile oluşturacağız

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user; // Hangi kullanıcıya ait?

    private LocalDateTime expiryDate; // 15 dakika sonra geçerliliğini yitirecek
}
