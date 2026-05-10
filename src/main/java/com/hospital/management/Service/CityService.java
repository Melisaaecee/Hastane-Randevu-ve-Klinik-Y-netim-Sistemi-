package com.hospital.management.Service;

import com.hospital.management.Entity.City;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

 
    // Tüm şehirleri getir
    public List<City> getAllCities() {
        return cityRepository.findAllByOrderByNameAsc(); 
    }

    // Şehir ID'sine göre şehir bilgisi getirir.
    public City getById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Şehir bulunamadı (ID: " + id + ")"));
    }

    // Yeni şehir ekler veya mevcut şehri günceller.
    @Transactional
    public City saveOrUpdateCity(City city) {
        return cityRepository.save(city);
    }

    // Şehri sistemden siler.
    @Transactional
    public void deleteCity(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new EntityNotFoundException("Silinmek istenen şehir bulunamadı (ID: " + id + ")");
        }
        cityRepository.deleteById(id);
    }
}