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

    // --- LİSTELEME VE DOĞRULAMA (HERKESE AÇIK) ---

    
    // Belirli bir şehre (City) ait tüm ilçeleri listeler.
    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<District>> getDistrictsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(districtService.getDistrictsByCity(cityId));
    }

    
    // ID ile ilçe sorgular.
    @GetMapping("/{id}")
    public ResponseEntity<District> getById(@PathVariable Long id) {
        return ResponseEntity.ok(districtService.getById(id));
    }

    
    // İlçe ve şehir eşleşmesini doğrular. (Randevu akışında hastaların kullanımı için uygundur).
    @GetMapping("/validate")
    public ResponseEntity<String> validateDistrictInCity(
            @RequestParam Long districtId,
            @RequestParam Long cityId) {
        districtService.validateDistrictInCity(districtId, cityId);
        return ResponseEntity.ok("İlçe ve şehir eşleşmesi doğrulandı.");
    }

    // --- YÖNETİMSEL İŞLEMLER (SADECE ADMIN) ---

    // Yeni bir ilçe ekler.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<District> create(@RequestBody District district) {
        return ResponseEntity.ok(districtService.saveOrUpdate(district));
    }

    
    // Belirli bir ilçeyi günceller.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<District> update(@PathVariable Long id, @RequestBody District district) {
        district.setId(id); // Güvenlik için URL'deki ID'yi modele set ediyoruz.
        return ResponseEntity.ok(districtService.saveOrUpdate(district));
    }

    // Belirli bir ilçeyi siler.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        districtService.delete(id);
        return ResponseEntity.ok("İlçe başarıyla silindi.");
    }
}