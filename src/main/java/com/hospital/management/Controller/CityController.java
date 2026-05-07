package com.hospital.management.Controller;

import com.hospital.management.Entity.City;
import com.hospital.management.Service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CityController {

    private final CityService cityService;

    // 🌍 HERKESE AÇIK: Randevu alırken herkes şehir seçebilmeli
    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    // 🌍 HERKESE AÇIK: Detaylı bilgi
    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getById(id));
    }

    // 🛡️ SADECE ADMIN: Yeni şehir ekleme
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<City> createCity(@RequestBody City city) {
        return ResponseEntity.ok(cityService.saveOrUpdateCity(city));
    }

    // 🛡️ SADECE ADMIN: Şehir silme
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.ok().build();
    }
}