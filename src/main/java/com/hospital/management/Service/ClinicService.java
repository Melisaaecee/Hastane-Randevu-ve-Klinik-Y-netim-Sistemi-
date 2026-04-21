package com.hospital.management.Service;

import com.hospital.management.Entity.Clinic;
import com.hospital.management.Repository.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;

    /**
     * 1. Tüm klinikleri listeler.
     */
    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    /**
     * 2. Belirli bir hastaneye ait tüm klinikleri ALFABETİK sıralı getirir.
     * Kullanıcı arayüzünde (Frontend) bölümlerin düzenli görünmesini sağlar.
     */
    public List<Clinic> getClinicsByHospitalId(Long hospitalId) {
        List<Clinic> clinics = clinicRepository.findByHospitalId(hospitalId);

        if (clinics.isEmpty()) {
            throw new RuntimeException("Bu hastaneye ait kayıtlı klinik bulunamadı.");
        }

        return clinics.stream()
                .sorted(Comparator.comparing(Clinic::getName)) // İsme göre A-Z sırala
                .collect(Collectors.toList());
    }

    /**
     * 3. Hastane içinde isme göre klinik araması yapar.
     */
    public List<Clinic> searchClinicsInHospital(Long hospitalId, String name) {
        return clinicRepository.findByHospitalIdAndNameContainingIgnoreCase(hospitalId, name);
    }

    /**
     * 4. Yeni klinik ekler veya mevcut kliniği günceller.
     */
    public Clinic saveOrUpdateClinic(Clinic clinic) {
        return clinicRepository.save(clinic);
    }

    /**
     * 5. Kliniği sistemden siler.
     */
    public void deleteClinic(Long id) {
        if (!clinicRepository.existsById(id)) {
            throw new RuntimeException("Silinmek istenen klinik bulunamadı!");
        }
        clinicRepository.deleteById(id);
    }

    /**
     * 6. Belirli bir hastanedeki toplam klinik sayısını döner.
     */
    public long getClinicCountByHospital(Long hospitalId) {
        return clinicRepository.countByHospitalId(hospitalId);
    }
}
