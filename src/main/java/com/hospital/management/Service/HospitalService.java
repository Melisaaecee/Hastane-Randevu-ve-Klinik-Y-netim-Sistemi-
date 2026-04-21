package com.hospital.management.Service;

import com.hospital.management.Entity.Hospital;
import com.hospital.management.Repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    /**
     * Tüm hastaneleri alfabetik (A-Z) olarak getirir.
     */
    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .sorted(Comparator.comparing(Hospital::getName))
                .collect(Collectors.toList());
    }


    /**
     * Seçilen ilçedeki hastaneleri alfabetik sıralı getirir.
     * Randevu akışında ilçe seçiminden sonra kullanılır.
     */
    public List<Hospital> getHospitalsByDistrict(Long districtId) {
        List<Hospital> hospitals = hospitalRepository.findByDistrictId(districtId);
        
        return hospitals.stream()
                .sorted(Comparator.comparing(Hospital::getName))
                .collect(Collectors.toList());
    }

    /**
     * Şehirdeki tüm hastaneleri (ilçe fark etmeksizin) alfabetik getirir.
     */
    public List<Hospital> getHospitalsByCity(Long cityId) {
        return hospitalRepository.findByDistrictCityId(cityId).stream()
                .sorted(Comparator.comparing(Hospital::getName))
                .collect(Collectors.toList());
    }

    /**
     * İlçe içinde isme göre hastane filtrelemesi yapar.
     */
    public List<Hospital> searchHospitalsInDistrict(Long districtId, String name) {
        return hospitalRepository.findByDistrictIdAndNameContainingIgnoreCase(districtId, name);
    }

    /**
     * Yeni hastane ekler veya mevcut olanı günceller.
     */
    @Transactional
    public Hospital saveOrUpdate(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }

    /**
     * Hastaneyi sistemden siler.
     */
    @Transactional
    public void deleteHospital(Long id) {
        if (!hospitalRepository.existsById(id)) {
            throw new RuntimeException("Silinecek hastane bulunamadı!");
        }
        hospitalRepository.deleteById(id);
    }
}