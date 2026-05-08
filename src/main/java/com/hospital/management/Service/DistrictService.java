package com.hospital.management.Service;

import com.hospital.management.Entity.District;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;

    public List<District> getDistrictsByCity(Long cityId) {
        return districtRepository.findByCityId(cityId).stream()
                .sorted(Comparator.comparing(District::getName))
                .toList();
    }

    public District getById(Long id) {
        return districtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("İlçe bulunamadı (ID: " + id + ")"));
    }

    public void validateDistrictInCity(Long districtId, Long cityId) {
        boolean exists = districtRepository.existsByIdAndCityId(districtId, cityId);
        if (!exists) {
            throw new BadRequestException("Seçilen ilçe belirtilen şehre ait değil!");
        }
    }

    @Transactional
    public District saveOrUpdate(District district) {
        if (district.getId() != null && !districtRepository.existsById(district.getId())) {
            throw new EntityNotFoundException("Güncellenmek istenen ilçe bulunamadı.");
        }
        return districtRepository.save(district);
    }

    @Transactional
    public void delete(Long id) {
        if (!districtRepository.existsById(id)) {
            throw new EntityNotFoundException("Silinmek istenen ilçe bulunamadı.");
        }
        districtRepository.deleteById(id);
    }
}