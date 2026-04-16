package com.hospital.management.Repository;

import com.hospital.management.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Giriş işlemi (Login) için kullanıcı adı ile bulma
    Optional<User> findByUsername(String username);

    // 2. TCKN ile kullanıcı bulma (Şifre sıfırlama veya kayıt kontrolü için)
    Optional<User> findByTckn(String tckn);

    // 3. E-posta ile kullanıcı bulma
    Optional<User> findByEmail(String email);

    // 4. Kayıt sırasında benzersizlik kontrolleri için
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByTckn(String tckn);
}
