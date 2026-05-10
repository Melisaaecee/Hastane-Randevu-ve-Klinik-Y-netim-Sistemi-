package com.hospital.management.Repository;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

        // Hastanın tüm randevuları
        List<Appointment> findByPatientId(Long patientId);

        // Hastanın duruma göre randevuları
        List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

        // Hastanın geçmiş randevuları
        List<Appointment> findByPatientIdAndSlotStartTimeBefore(Long patientId, LocalDateTime now);

        // Doktorun geçmiş randevuları
        List<Appointment> findBySlotDoctorIdAndSlotStartTimeBefore(Long doctorId, LocalDateTime now);

        // Hasta istatistik
        long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);

        // Klinik randevuları
        List<Appointment> findBySlotDoctorClinicId(Long clinicId);

        // Doktorun aktif randevuları
        List<Appointment> findBySlotDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

        // Doktorun tüm randevuları
        List<Appointment> findBySlotDoctorId(Long doctorId);

        // Hastanın o saatte başka randevusu var mı?
        boolean existsByPatientIdAndSlotStartTime(Long patientId, LocalDateTime startTime);

        // Doktor randevuları - tüm ilişkiler fetch
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

        // Hasta randevuları
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