package com.hospital.management.Repository;

import com.hospital.management.Entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    // 1. Belirli bir hastaneye ait tüm klinikleri getirir (En çok bu kullanılacak)
    List<Clinic> findByHospitalId(Long hospitalId);

    // 2. Belirli bir hastanede, isme göre klinik arama
    // Örn: "Ankara Şehir Hastanesi" içindeki "Kardiyoloji" kliniği
    List<Clinic> findByHospitalIdAndNameContainingIgnoreCase(Long hospitalId, String name);

    // 3. Bir hastanedeki toplam klinik sayısını verir
    long countByHospitalId(Long hospitalId);
}
