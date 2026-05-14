package com.hospital.management.Controller;

import com.hospital.management.DTO.DoctorProfileRequest;
import com.hospital.management.Entity.Doctor;
import com.hospital.management.Service.DoctorService;
import com.hospital.management.Service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor

public class DoctorController {

    private final DoctorService doctorService;

    // --- LİSTELEME ---
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<Doctor>> getDoctorsInClinic(@PathVariable Long clinicId) {
        return ResponseEntity.ok(doctorService.getAllDoctorsInClinic(clinicId));
    }

    @GetMapping("/specialization")
    public ResponseEntity<List<Doctor>> getBySpecialization(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(name));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctor(
            @RequestParam Long clinicId,
            @RequestParam String name) {
        return ResponseEntity.ok(doctorService.searchDoctorInClinic(clinicId, name));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Doctor> getDoctorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(doctorService.getDoctorByUserId(userId));
    }

    // --- DOKTOR OLUŞTURMA (SADECE ADMIN) ---
    @PostMapping("/create-with-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createDoctorWithUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String specialization,
            @RequestParam Long clinicId) {
  
            return ResponseEntity.ok(doctorService.createDoctorWithUser(firstName, lastName, specialization, clinicId));
    }

    // --- GÜNCELLEME ---
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Doctor> saveOrUpdate(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.saveOrUpdateDoctor(doctor));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoctorProfileRequest request) {

        doctorService.updateDoctorOwnProfile(userDetails.getUsername(), request);

        return ResponseEntity.ok("Profil başarıyla güncellendi");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}