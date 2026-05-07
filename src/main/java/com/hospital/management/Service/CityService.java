package com.hospital.management.Service;

import com.hospital.management.Entity.City;
import com.hospital.management.Repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    public List<City> getAllCities() {
        return cityRepository.findAllByOrderByNameAsc();
    }

    public City getById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Şehir bulunamadı: " + id));
    }

    // 🔥 YENİ: Şehir Ekleme veya Güncelleme
    public City saveOrUpdateCity(City city) {
        return cityRepository.save(city);
    }

    // 🔥 YENİ: Şehir Silme
    public void deleteCity(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new RuntimeException("Silinmek istenen şehir bulunamadı!");
        }
        cityRepository.deleteById(id);
    }
}