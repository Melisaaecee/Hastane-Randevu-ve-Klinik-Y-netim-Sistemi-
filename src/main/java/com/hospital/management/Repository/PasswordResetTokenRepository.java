package com.hospital.management.Repository;

import com.hospital.management.Entity.PasswordResetToken;
import com.hospital.management.Entity.User; // User entity'nin yerini kontrol et
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    // Token üzerinden kaydı bulmak için
    Optional<PasswordResetToken> findByToken(String token);
    
    // Kullanıcıya ait eski tokenları temizlemek için
    void deleteByUser(User user);
}