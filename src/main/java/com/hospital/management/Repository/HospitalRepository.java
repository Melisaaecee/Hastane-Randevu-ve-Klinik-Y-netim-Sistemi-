package com.hospital.management.Repository;

import com.hospital.management.Entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 1. Belirli bir ilçeye ait tüm hastaneleri listeler
    // (İlçe seçildikten sonra hastane dropdown listesini doldurmak için)
    List<Hospital> findByDistrictId(Long districtId);

    // 2. Bir ilçe içindeki hastaneleri ismine göre filtrelemek için
    // (Örn: Kadıköy ilçesindeki içinde "Devlet" geçen hastaneler)
    List<Hospital> findByDistrictIdAndNameContainingIgnoreCase(Long districtId, String name);

    // 3. Şehir ID'sine göre hastaneleri getirir 
    // (İlçe bağımsız, direkt şehirdeki tüm hastaneleri görmek istersen)
    List<Hospital> findByDistrictCityId(Long cityId);

<<<<<<< HEAD
   
=======
    
>>>>>>> f88a7626b659710698962284be6ac7c666dfe2f9
}
