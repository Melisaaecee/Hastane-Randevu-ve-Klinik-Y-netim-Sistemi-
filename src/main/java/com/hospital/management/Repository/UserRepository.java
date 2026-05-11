package com.hospital.management.Repository;

import com.hospital.management.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        // BAŞARISIZ DENEMELER İÇİN GÜNCELLEME (transactional eklendi)
        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.failedAttempt = :failedAttempt, u.accountNonLocked = :accountNonLocked, u.lockTime = :lockTime WHERE u.id = :id")
        void updateFailedAttempts(@Param("id") Long id,
                        @Param("failedAttempt") Integer failedAttempt,
                        @Param("accountNonLocked") Boolean accountNonLocked,
                        @Param("lockTime") LocalDateTime lockTime);

        // SADECE failed_attempt güncelleme (daha basit)
        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.failedAttempt = :failedAttempt WHERE u.id = :id")
        void incrementFailedAttempt(@Param("id") Long id, @Param("failedAttempt") Integer failedAttempt);

        // Hesap kilitleme
        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.accountNonLocked = false, u.lockTime = :lockTime WHERE u.id = :id")
        void lockAccount(@Param("id") Long id, @Param("lockTime") LocalDateTime lockTime);

        // Hesap kilidini açma
        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.accountNonLocked = true, u.failedAttempt = 0, u.lockTime = null WHERE u.id = :id")
        void unlockAccount(@Param("id") Long id);
}