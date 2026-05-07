package com.hospital.management.Controller;

import com.hospital.management.Entity.District;
import com.hospital.management.Service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DistrictController {

    private final DistrictService districtService;

    // --- OKUMA İŞLEMLERİ (HERKESE AÇIK) ---

    @GetMapping("/city/{cityId}")
    public List<District> getDistrictsByCity(@PathVariable Long cityId) {
        return districtService.getDistrictsByCity(cityId);
    }

    @GetMapping("/{id}")
    public District getById(@PathVariable Long id) {
        return districtService.getById(id);
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateDistrictInCity(@RequestParam Long districtId,
                                                       @RequestParam Long cityId) {
        districtService.validateDistrictInCity(districtId, cityId);
        return ResponseEntity.ok("Valid district-city match");
    }

    // --- YAZMA İŞLEMLERİ (SADECE ADMIN) ---

    /**
     * Yeni bir ilçe ekler.
     * POST http://localhost:8080/api/districts
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<District> createDistrict(@RequestBody District district) {
        return ResponseEntity.ok(districtService.saveOrUpdate(district));
    }

    /**
     * Mevcut bir ilçeyi günceller.
     * PUT http://localhost:8080/api/districts/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<District> updateDistrict(@PathVariable Long id, @RequestBody District district) {
        district.setId(id); // URL'deki ID'nin entity'e set edildiğinden emin oluyoruz
        return ResponseEntity.ok(districtService.saveOrUpdate(district));
    }

    /**
     * Bir ilçeyi siler.
     * DELETE http://localhost:8080/api/districts/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDistrict(@PathVariable Long id) {
        districtService.delete(id);
        return ResponseEntity.ok("İlçe başarıyla silindi.");
    }
}