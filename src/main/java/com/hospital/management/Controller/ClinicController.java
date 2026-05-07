package com.hospital.management.Controller;

import com.hospital.management.Entity.Clinic;
import com.hospital.management.Service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Frontend entegrasyonu için
public class ClinicController {

    private final ClinicService clinicService;

    /**
     * Tüm klinikleri listeler.
     * GET http://localhost:8080/api/clinics
     */
    @GetMapping
    public ResponseEntity<List<Clinic>> getAllClinics() {
        return ResponseEntity.ok(clinicService.getAllClinics());
    }

    /**
     * Belirli bir hastaneye ait klinikleri alfabetik getirir.
     * Randevu akışında hastane seçildikten sonra poliklinik listesini doldurur.
     * GET http://localhost:8080/api/clinics/hospital/{hospitalId}
     */
    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<Clinic>> getClinicsByHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(clinicService.getClinicsByHospitalId(hospitalId));
    }

    /**
     * Hastane içinde isme göre klinik arar (Örn: "Göz", "Dahiliye").
     * GET http://localhost:8080/api/clinics/search?hospitalId=1&name=Göz
     */
    @GetMapping("/search")
    public ResponseEntity<List<Clinic>> searchClinics(
            @RequestParam Long hospitalId, 
            @RequestParam String name) {
        return ResponseEntity.ok(clinicService.searchClinicsInHospital(hospitalId, name));
    }

    /**
     * Belirli bir hastanedeki toplam klinik sayısını döner.
     * GET http://localhost:8080/api/clinics/count/{hospitalId}
     */
    @GetMapping("/count/{hospitalId}")
    public ResponseEntity<Long> getClinicCount(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(clinicService.getClinicCountByHospital(hospitalId));
    }

    /**
     * Yeni klinik oluşturur veya günceller (Admin Paneli için).
     * POST http://localhost:8080/api/clinics
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Clinic> saveOrUpdate(@RequestBody Clinic clinic) {
        return ResponseEntity.ok(clinicService.saveOrUpdateClinic(clinic));
    }

    /**
     * Kliniği siler.
     * DELETE http://localhost:8080/api/clinics/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.ok("Klinik başarıyla silindi.");
    }
}
