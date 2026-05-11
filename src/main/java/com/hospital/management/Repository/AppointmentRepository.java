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

    List<Appointment> findByPatientId(Long patientId);

    // DOKTOR VE KLİNİK BİLGİSİNİ TAM GETİREN KRİTİK SORGULAR
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.slot s " +
           "JOIN FETCH s.doctor d " +
           "JOIN FETCH d.user u " +
           "JOIN FETCH d.clinic c " +
           "WHERE a.patient.user.id = :userId")
    List<Appointment> findAllByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.slot s " +
           "JOIN FETCH s.doctor d " +
           "JOIN FETCH d.user u " +
           "JOIN FETCH d.clinic c " +
           "WHERE a.patient.user.id = :userId AND s.startTime < :now")
    List<Appointment> findPastByUserIdWithDetails(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.slot s " +
           "JOIN FETCH s.doctor d " +
           "JOIN FETCH d.clinic c " +
           "JOIN FETCH c.hospital h " +
           "WHERE a.patient.id = :patientId " +
           "AND s.startTime = :startTime " +
           "AND a.status = com.hospital.management.Entity.AppointmentStatus.APPROVED")
    Optional<Appointment> findConflictDetail(@Param("patientId") Long patientId, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND CAST(a.slot.startTime AS date) = CAST(:date AS date) " +
           "AND a.status = com.hospital.management.Entity.AppointmentStatus.APPROVED")
    boolean hasAnyAppointmentOnDate(@Param("patientId") Long patientId, @Param("date") LocalDateTime date);

    // Diğer yardımcı metodlar aynen kalıyor
    List<Appointment> findBySlotDoctorId(Long doctorId);
    List<Appointment> findBySlotDoctorClinicId(Long clinicId);
    List<Appointment> findBySlotDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
}