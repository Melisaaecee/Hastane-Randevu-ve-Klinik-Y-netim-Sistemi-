package com.hospital.management.Repository;

import com.hospital.management.Entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // 1. Belirli bir klinikte çalışan tüm doktorları listeler
    // (Poliklinik seçildikten sonra doktor listesini doldurmak için)
    List<Doctor> findByClinicId(Long clinicId);

    // 2. Uzmanlık alanına göre doktorları filtrelemek istersen
    List<Doctor> findBySpecialization(String specialization);

    // 3. User tablosundaki User ID'ye göre doktoru bulur
    // (Giriş yapan doktorun kendi bilgilerini görmesi için)
    Optional<Doctor> findByUserId(Long userId);

    // 4. Belirli bir klinikte, doktorun adına göre arama yapmak için
    // (Not: Doctor içindeki User nesnesinin firstName alanına erişir)
    List<Doctor> findByClinicIdAndUserFirstNameContainingIgnoreCase(Long clinicId, String firstName);
}
