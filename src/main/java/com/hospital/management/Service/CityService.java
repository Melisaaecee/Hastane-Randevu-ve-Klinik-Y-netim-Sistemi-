package com.hospital.management.Service;

import com.hospital.management.Entity.City;
import com.hospital.management.Repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    // Tüm şehirleri alfabetik (A-Z) getirir
    public List<City> getAllCities() {
        return cityRepository.findAll().stream()
                .sorted(Comparator.comparing(City::getName))
                .toList();
    }

    public City getById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Şehir bulunamadı"));
    }
}
