package com.hospital.management.Service;

import com.hospital.management.Entity.District;
import com.hospital.management.Repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;

    //  GET BY CITY
    // Seçilen şehre ait ilçeleri alfabetik (A-Z) getirir
    public List<District> getDistrictsByCity(Long cityId) {
        return districtRepository.findByCityId(cityId).stream()
                .sorted(Comparator.comparing(District::getName))
                .toList();
    }

   
    //  GET BY ID
    public District getById(Long id) {
        return districtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İlçe bulunamadı"));
    }

    //  CRITICAL VALIDATION 
    /**
     * Bir ilçenin gerçekten belirtilen şehre ait olup olmadığını kontrol eder.
     * Randevu alırken veya hastane eklerken yanlış eşleşmeleri önler.
     */
    public void validateDistrictInCity(Long districtId, Long cityId) {
        boolean exists = districtRepository.existsByIdAndCityId(districtId, cityId);
        if (!exists) {
            throw new RuntimeException("Seçilen ilçe belirtilen şehre ait değil!");
        }
    }
}
