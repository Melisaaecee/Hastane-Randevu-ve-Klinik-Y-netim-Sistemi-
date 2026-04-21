package com.hospital.management.Repository;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Hastanın tüm randevuları
    List<Appointment> findByPatientId(Long patientId);

    // Hastanın duruma göre randevuları
    List<Appointment> findByPatientId(Long patientId, AppointmentStatus status);

    // Hastanın geçmiş randevuları
    List<Appointment> findByPatientIdAndSlotStartTimeBefore(Long patientId, LocalDateTime now);

    // Doktorun geçmiş randevuları
    List<Appointment> findBySlotDoctorIdAndSlotStartTimeBefore(Long doctorId, LocalDateTime now);

    // Hasta istatistik (gelmedi sayısı vs.)
    long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    // Klinik randevuları (admin)
    List<Appointment> findBySlotDoctorClinicId(Long clinicId);

    // Doktorun aktif randevuları
    List<Appointment> findBySlotDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    // Doktorun tüm randevuları
    List<Appointment> findBySlotDoctorId(Long doctorId);
}