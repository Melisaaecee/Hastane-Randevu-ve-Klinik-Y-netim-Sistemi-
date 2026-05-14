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

public class ClinicController {

    private final ClinicService clinicService;

   

    // Tüm klinikleri listeler.
    @GetMapping
    public ResponseEntity<List<Clinic>> getAllClinics() {
        return ResponseEntity.ok(clinicService.getAllClinics());
    }

    // Belirli bir hastaneye (Hospital) ait tüm klinikleri listeler.
    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<Clinic>> getClinicsByHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(clinicService.getClinicsByHospitalId(hospitalId));
    }

    // Hastane içinde isme göre klinik araması yapar
    @GetMapping("/search")
    public ResponseEntity<List<Clinic>> searchClinics(
            @RequestParam Long hospitalId,
            @RequestParam String name) {
        return ResponseEntity.ok(clinicService.searchClinicsInHospital(hospitalId, name));
    }

    // Belirli bir hastaneye ait klinik sayısını getirir
    @GetMapping("/hospital/{hospitalId}/count")
    public ResponseEntity<Long> getClinicCount(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(clinicService.getClinicCountByHospital(hospitalId));
    }

    // --- YÖNETİMSEL İŞLEMLER (SADECE ADMIN) ---

    // yeni bir klinik ekler veya mevcut bir kliniği günceller.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Clinic> saveOrUpdate(@RequestBody Clinic clinic) {
        return ResponseEntity.ok(clinicService.saveOrUpdateClinic(clinic));
    }

    // Belirli bir kliniği siler.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.ok("Klinik başarıyla silindi.");
    }
}