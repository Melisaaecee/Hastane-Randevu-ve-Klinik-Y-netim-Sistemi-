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

    // --- LİSTELEME İŞLEMLERİ (HERKESE AÇIK) ---

    // Tüm şehirleri listeler.
    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    // Belirli bir ID'ye sahip şehri getirir.
    @GetMapping("/{id}")
    public ResponseEntity<City> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getById(id));
    }

    // --- YÖNETİMSEL İŞLEMLER (SADECE ADMIN) ---

    // Yeni bir şehir ekler veya mevcut bir şehri günceller.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<City> saveOrUpdate(@RequestBody City city) {
        return ResponseEntity.ok(cityService.saveOrUpdateCity(city));
    }

   
    // Belirli bir şehri siler.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.ok("Şehir başarıyla silindi.");
    }
}