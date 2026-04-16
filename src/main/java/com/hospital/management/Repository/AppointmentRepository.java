package com.hospital.management.Repository;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Hastanın tüm randevuları
    List<Appointment> findByPatientId(Long patientId);

    // Hastanın duruma göre randevuları
    List<Appointment> findByPatientId(Long patientId, AppointmentStatus status);

    // 3. Slot dolu mu?
    Optional<Appointment> findBySlotId(Long slotId);

    // Hasta istatistik (gelmedi sayısı vs.)
    long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    // Klinik randevuları (admin)
    List<Appointment> findBySlotDoctorClinicId(Long clinicId);

    // Doktorun aktif randevuları
    List<Appointment> findBySlotDoctorIdAndStatus(Long doctorId,  List<AppointmentStatus> statuses);

    // Doktorun tüm randevuları
    List<Appointment> findBySlotDoctorId(Long doctorId);
}