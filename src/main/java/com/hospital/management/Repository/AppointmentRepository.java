package com.hospital.management.Repository;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 1. Bir hastanın tüm randevularını listelemek için (Randevularım sayfası)
    List<Appointment> findByPatientId(Long patientId);

    // 2. Bir hastanın durumuna göre randevularını getir (Örn: Sadece Onaylı olanlar)
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    // 3. Bir slot ID'sine göre randevu bul (Slot dolu mu kontrolü için gerekebilir)
    Appointment findBySlotId(Long slotId);

    // 4. Hastanın kaç kere randevusuna gelmediğini saymak için (İstatistik veya ceza için)
    long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);
}
