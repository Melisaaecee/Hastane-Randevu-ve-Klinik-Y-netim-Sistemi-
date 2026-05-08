package com.hospital.management.Controller;

import com.hospital.management.Entity.Hospital;
import com.hospital.management.Service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HospitalController {

    private final HospitalService hospitalService;

    // --- LİSTELEME VE SORGULAMA (HERKESE AÇIK) ---

    // Tüm hastaneleri listeler.
    @GetMapping
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    // Belirli bir ilçeye (District) ait tüm hastaneleri listeler.
    @GetMapping("/district/{districtId}")
    public ResponseEntity<List<Hospital>> getHospitalsByDistrict(@PathVariable Long districtId) {
        return ResponseEntity.ok(hospitalService.getHospitalsByDistrict(districtId));
    }

   
    // Belirli bir ile (City) ait tüm hastaneleri listeler.
    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<Hospital>> getHospitalsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(hospitalService.getHospitalsByCity(cityId));
    }

   
    // İlçe ve hastane adına göre arama yapar.
    @GetMapping("/search")
    public ResponseEntity<List<Hospital>> searchHospitals(
            @RequestParam Long districtId,
            @RequestParam String name) {
        return ResponseEntity.ok(hospitalService.searchHospitalsInDistrict(districtId, name));
    }

    // --- YÖNETİMSEL İŞLEMLER (SADECE ADMIN) ---

    // Yeni bir hastane ekler veya mevcut bir hastaneyi günceller.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hospital> saveOrUpdate(@RequestBody Hospital hospital) {
        return ResponseEntity.ok(hospitalService.saveOrUpdate(hospital));
    }

    
    // Belirli bir hastaneyi siler.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteHospital(@PathVariable Long id) {
        hospitalService.deleteHospital(id);
        return ResponseEntity.ok("Hastane başarıyla silindi.");
    }
}