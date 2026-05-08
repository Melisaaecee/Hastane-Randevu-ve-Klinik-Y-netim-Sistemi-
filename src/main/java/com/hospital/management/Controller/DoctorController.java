package com.hospital.management.Controller;

import com.hospital.management.Entity.Doctor;
import com.hospital.management.Service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    // --- LİSTELEME VE FİLTRELEME (HERKESE AÇIK) ---


    // Tüm doktorları listeler.
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

   // Klinik içindeki tüm doktorları listeler.
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<Doctor>> getDoctorsInClinic(@PathVariable Long clinicId) {
        return ResponseEntity.ok(doctorService.getAllDoctorsInClinic(clinicId));
    }

   
    // Uzmanlık adına göre doktorları listeler.
    @GetMapping("/specialization")
    public ResponseEntity<List<Doctor>> getBySpecialization(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(name));
    }

   
    // Klinik ve doktora adına göre arama yapar.
    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctor(
            @RequestParam Long clinicId,
            @RequestParam String name) {
        return ResponseEntity.ok(doctorService.searchDoctorInClinic(clinicId, name));
    }

    // --- ÖZEL ERİŞİM VE YÖNETİM ---


    //userId ile doktor sorgular.
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Doctor> getDoctorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(doctorService.getDoctorByUserId(userId));
    }

    // Yeni bir doktor ekler veya mevcut bir doktoru günceller.
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Doctor> saveOrUpdate(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.saveOrUpdateDoctor(doctor));
    }

    // Doktor kaydını sistemden tamamen siler.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok("Doktor kaydı başarıyla silindi.");
    }
}