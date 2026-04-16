package com.hospital.management.Repository;

import com.hospital.management.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // 1.. User tablosundaki User ID'ye göre hastayı bulur
    // Sisteme giriş yapan kişinin "Patient" detaylarına (kan grubu, cezalar vb.) 
    // ulaşmak için en çok bu metodu kullanacaksın.
    Optional<Patient> findByUserId(Long userId);

    // 2. TCKN üzerinden hastayı bulmak istersen (User tablosuyla join yapar)
    Optional<Patient> findByUserTckn(String tckn);

    // 4. Bir hastanın sistemde kayıtlı olup olmadığını TCKN ile kontrol etmek için
    boolean existsByUserTckn(String tckn);
}
