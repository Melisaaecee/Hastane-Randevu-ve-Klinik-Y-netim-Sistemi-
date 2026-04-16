package com.hospital.management.Repository;

import com.hospital.management.Entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    // 1. Şehir ID'sine göre tüm ilçeleri listeler (Hiyerarşik seçim için en kritik metot)
    List<District> findByCityId(Long cityId);

    // 2. Şehir ID'sine ve ilçe ismine göre arama yapar
    // Örn: "İstanbul" şehri içindeki "Beşiktaş" ilçesini bulmak için
    List<District> findByCityIdAndNameContainingIgnoreCase(Long cityId, String name);

    // 3. Bir şehrin toplam kaç ilçesi olduğunu döndürür
    long countByCityId(Long cityId);
}