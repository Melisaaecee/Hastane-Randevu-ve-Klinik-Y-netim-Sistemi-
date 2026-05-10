package com.hospital.management.Service;

import com.hospital.management.Entity.Hospital;
import com.hospital.management.Exception.EntityNotFoundException;
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

    // Tüm hastaneleri getir 
    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAllWithDetails(); // ✅ Fetch join
    }

    // İlçedeki hastaneleri getir 
    public List<Hospital> getHospitalsByDistrict(Long districtId) {
        return hospitalRepository.findByDistrictIdWithDetails(districtId); // ✅ Fetch join
    }

    // Şehirdeki tüm hastaneler
    public List<Hospital> getHospitalsByCity(Long cityId) {
        return hospitalRepository.findByDistrictCityId(cityId).stream()
                .sorted(Comparator.comparing(Hospital::getName))
                .collect(Collectors.toList());
    }

    // Filtreleme
    public List<Hospital> searchHospitalsInDistrict(Long districtId, String name) {
        return hospitalRepository.findByDistrictIdAndNameContainingIgnoreCase(districtId, name);
    }

    // Kaydet / Güncelle
    @Transactional
    public Hospital saveOrUpdate(Hospital hospital) {
        // Not: Controller katmanında Admin kontrolü yapılması önerilir.
        return hospitalRepository.save(hospital);
    }

    // Sil
    @Transactional
    public void deleteHospital(Long id) {
        if (!hospitalRepository.existsById(id)) {
            throw new EntityNotFoundException("Silinmek istenen hastane sistemde bulunamadı (ID: " + id + ")");
        }
        hospitalRepository.deleteById(id);
    }
}