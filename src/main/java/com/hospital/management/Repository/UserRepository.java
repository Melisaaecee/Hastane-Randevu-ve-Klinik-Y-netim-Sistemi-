package com.hospital.management.Repository;

import com.hospital.management.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Giriş işlemi (Login) için kullanıcı adı ile bulma
    Optional<User> findByUsername(String username);

    // 2. TCKN ile kullanıcı bulma
    Optional<User> findByTckn(String tckn);

    // 3. E-posta ile kullanıcı bulma
    Optional<User> findByEmail(String email);

    // 4. Kayıt sırasında benzersizlik kontrolleri için
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByTckn(String tckn);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.patient p " +
            "LEFT JOIN FETCH u.doctor d " +
            "LEFT JOIN FETCH d.clinic c " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.patient p " +
            "LEFT JOIN FETCH u.doctor d " +
            "WHERE u.tckn = :tckn")
    Optional<User> findByTcknWithDetails(@Param("tckn") String tckn);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.patient p " +
            "LEFT JOIN FETCH u.doctor d " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithDetails(@Param("username") String username);
}
