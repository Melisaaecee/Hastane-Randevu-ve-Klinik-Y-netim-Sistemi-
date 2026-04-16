package com.hospital.management.Repository;

import com.hospital.management.Entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    // 1. Hastanın belirli bir klinikte aktif bir cezası var mı? (En Kritik Sorgu)
    // endDate şu anki tarihten (now) büyükse ve active true ise ceza devam
    // ediyordur.
    boolean existsByPatientIdAndClinicIdAndPenaltyEndDateAfterAndActiveTrue(Long patientId, Long clinicId,
            LocalDateTime now);

    // aktif ceza var mı?
    boolean existsByPatientIdAndActiveTrue(Long patientId);

    // 2. Bir hastaya ait tüm ceza geçmişini listeler
    List<Penalty> findByPatientId(Long patientId);

    // 3. Hastanın aktif olan tüm cezalarını listeler
    List<Penalty> findByPatientIdAndActiveTrue(Long patientId);

    // 4. (admin) tüm geçmiş cezalarını listeler
    List<Penalty> findByPenaltyEndDateBefore(LocalDateTime now);

    // 5. bir hastanın geçmiş cezalarını listeler (aktif olmayan veya süresi dolmuş
    // cezalar)
    List<Penalty> findByPatientIdAndPenaltyEndDateBefore(Long patientId, LocalDateTime now);

    // 6. TCKN ile hastanın tüm cezalarını bulma
    // Bu metot Penalty içindeki Patient'a, oradan da User içindeki tckn alanına
    // gider.
    List<Penalty> findByPatientUserTckn(String tckn);

    // 7. Tarihe göre sıralı getirme (En yeni ceza en üstte)
    // Admin için daha düzenli bir görünüm sağlar.
    List<Penalty> findByPatientUserTcknOrderByPenaltyStartDateDesc(String tckn);
}
