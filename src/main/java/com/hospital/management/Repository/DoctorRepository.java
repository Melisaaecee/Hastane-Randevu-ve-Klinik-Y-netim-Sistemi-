package com.hospital.management.Repository;

import com.hospital.management.Entity.Doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        // City bilgisine district üzerinden eriş (district.city)
        @Query("SELECT DISTINCT d FROM Doctor d " +
                        "LEFT JOIN FETCH d.user " +
                        "LEFT JOIN FETCH d.clinic c " +
                        "LEFT JOIN FETCH c.hospital h " +
                        "LEFT JOIN FETCH h.district dist " +
                        "LEFT JOIN FETCH dist.city") // city buradan geliyor
        List<Doctor> findAllWithDetails();

        // ID ile
        @Query("SELECT DISTINCT d FROM Doctor d " +
                        "LEFT JOIN FETCH d.user " +
                        "LEFT JOIN FETCH d.clinic c " +
                        "LEFT JOIN FETCH c.hospital h " +
                        "LEFT JOIN FETCH h.district dist " +
                        "LEFT JOIN FETCH dist.city " +
                        "WHERE d.id = :id")
        Optional<Doctor> findByIdWithDetails(@Param("id") Long id);

        // User ID ile
        @Query("SELECT DISTINCT d FROM Doctor d " +
                        "LEFT JOIN FETCH d.user " +
                        "LEFT JOIN FETCH d.clinic c " +
                        "LEFT JOIN FETCH c.hospital h " +
                        "LEFT JOIN FETCH h.district dist " +
                        "LEFT JOIN FETCH dist.city " +
                        "WHERE d.user.id = :userId")
        Optional<Doctor> findByUserIdWithDetails(@Param("userId") Long userId);

        // Klinik ID ile
        @Query("SELECT DISTINCT d FROM Doctor d " +
                        "LEFT JOIN FETCH d.user " +
                        "LEFT JOIN FETCH d.clinic c " +
                        "LEFT JOIN FETCH c.hospital h " +
                        "LEFT JOIN FETCH h.district dist " +
                        "LEFT JOIN FETCH dist.city " +
                        "WHERE d.clinic.id = :clinicId")
        List<Doctor> findByClinicIdWithDetails(@Param("clinicId") Long clinicId);

        @Query("SELECT d FROM Doctor d WHERE d.user.username = :username")
        Optional<Doctor> findByUserUsername(@Param("username") String username);


}
