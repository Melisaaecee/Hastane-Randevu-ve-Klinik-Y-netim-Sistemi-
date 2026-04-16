package com.hospital.management.Repository;

import com.hospital.management.Entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    // 1. Hastanın belirli bir klinikte aktif bir cezası var mı? (En Kritik Sorgu)
    // endDate şu anki tarihten (now) büyükse ve active true ise ceza devam ediyordur.
    boolean existsByPatientIdAndClinicIdAndPenaltyEndDateAfterAndActiveTrue(Long patientId, Long clinicId, LocalDateTime now);

    // 2. Bir hastaya ait tüm ceza geçmişini listeler
    List<Penalty> findByPatientId(Long patientId);

    // 3. Belirli bir tarihten önce bitmiş ama hala "active=true" kalmış cezaları bulmak için
    // (Sistemde cezaları pasife çeken bir batch işi yazarsan kullanışlı olur)
    List<Penalty> findByPenaltyEndDateBeforeAndActiveTrue(LocalDateTime now);

    // 4. Hastanın aktif olan tüm cezalarını listeler
    List<Penalty> findByPatientIdAndActiveTrue(Long patientId);
}
