package com.hospital.management.Repository;

import com.hospital.management.Entity.Slot;
import com.hospital.management.Entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    // 1. Belirli bir doktorun, belirli bir durumdaki (Örn: AVAILABLE) tüm slotlarını getirir
    // Randevu alma sayfasında doktor seçildikten sonra boş saatleri göstermek için kullanılır.
    List<Slot> findByDoctorIdAndStatus(Long doctorId, SlotStatus status);

    // 2. Belirli bir doktorun, belirli bir tarih aralığındaki slotlarını getirir
    // Doktorun günlük veya haftalık çalışma takvimini görüntülemesi için.
    List<Slot> findByDoctorIdAndStartTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    // 3. Belirli bir doktorun, belirli bir durumdaki ve gelecekteki slotlarını listeler
    // Geçmişteki boş slotların randevu listesinde görünmesini engellemek için.
    List<Slot> findByDoctorIdAndStatusAndStartTimeAfter(Long doctorId, SlotStatus status, LocalDateTime now);

    // 4. Çakışma kontrolü: Doktorun o saatte başka bir slotu var mı?
    // Yeni slot eklerken uniqueConstraint hatası almamak için ön kontrol sağlar.
    boolean existsByDoctorIdAndStartTime(Long doctorId, LocalDateTime startTime);
}
