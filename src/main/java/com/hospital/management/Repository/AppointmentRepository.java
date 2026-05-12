package com.hospital.management.Repository;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // --- MEVCUT METODLARIN ---
    
    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    // Geçmiş (Bunu bu isimle ekle veya güncelle)
List<Appointment> findByPatient_User_IdAndSlot_StartTimeBefore(Long userId, LocalDateTime now);

// Aktif (Bunu bu isimle ekle veya güncelle)
    List<Appointment> findByPatient_User_IdAndSlot_StartTimeAfter(Long userId, LocalDateTime now);

    List<Appointment> findBySlotDoctorIdAndSlotStartTimeBefore(Long doctorId, LocalDateTime now);

    long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    List<Appointment> findBySlotDoctorClinicId(Long clinicId);

    List<Appointment> findBySlotDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    List<Appointment> findBySlotDoctorId(Long doctorId);


    // BU SATIRI EKLE: Verilen slotId ile bir randevu var mı yok mu kontrol eder
    boolean existsBySlotId(Long slotId);


    List<Appointment> findByPatient_User_Id(Long userId);

    // --- YENİ EKLENEN KRİTİK METODLAR ---

    /**
     * 1. KURAL: Aynı gün başka randevu var mı?
     * SQL'deki CAST(... AS date) ile zaman damgasının sadece tarih kısmını karşılaştırıyoruz.
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND CAST(a.slot.startTime AS date) = CAST(:date AS date) " +
           "AND a.status = com.hospital.management.Entity.AppointmentStatus.APPROVED")
    boolean hasAnyAppointmentOnDate(@Param("patientId") Long patientId, @Param("date") LocalDateTime date);


    
    /**
     * 2. KURAL: Aynı saatte çakışma detayını getir.
     * Kullanıcıya "Şu hastanede randevunuz var" diyebilmek için tüm ilişkileri Fetch ediyoruz.
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.slot s " +
           "JOIN FETCH s.doctor d " +
           "JOIN FETCH d.clinic c " +
           "JOIN FETCH c.hospital h " +
           "WHERE a.patient.id = :patientId " +
           "AND s.startTime = :startTime " +
           "AND a.status = com.hospital.management.Entity.AppointmentStatus.APPROVED")
    Optional<Appointment> findConflictDetail(@Param("patientId") Long patientId, @Param("startTime") LocalDateTime startTime);

    // --- FETCH JOIN SORGULARIN (DOKUNULMADI) ---

    @Query("SELECT DISTINCT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH a.slot s " +
            "LEFT JOIN FETCH s.doctor d " +
            "LEFT JOIN FETCH d.user " +
            "LEFT JOIN FETCH d.clinic c " +
            "LEFT JOIN FETCH c.hospital h " +
            "LEFT JOIN FETCH h.district dist " +
            "LEFT JOIN FETCH dist.city " +
            "WHERE s.doctor.id = :doctorId")
    List<Appointment> findByDoctorIdWithDetails(@Param("doctorId") Long doctorId);

    @Query("SELECT DISTINCT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH a.slot s " +
            "LEFT JOIN FETCH s.doctor d " +
            "LEFT JOIN FETCH d.user " +
            "LEFT JOIN FETCH d.clinic c " +
            "LEFT JOIN FETCH c.hospital h " +
            "LEFT JOIN FETCH h.district dist " +
            "LEFT JOIN FETCH dist.city " +
            "WHERE a.patient.id = :patientId")
    List<Appointment> findByPatientIdWithDetails(@Param("patientId") Long patientId);
}