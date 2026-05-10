package com.hospital.management.Controller;

import com.hospital.management.Entity.Doctor;
import com.hospital.management.Service.DoctorService;
import com.hospital.management.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;
    private final UserService userService;

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
    public ResponseEntity<?> createDoctorWithUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String specialization,
            @RequestParam Long clinicId) {
        try {
            return ResponseEntity.ok(doctorService.createDoctorWithUser(firstName, lastName, specialization, clinicId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- GÜNCELLEME ---
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Doctor> saveOrUpdate(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.saveOrUpdateDoctor(doctor));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates) {
        try {
            String tckn = SecurityContextHolder.getContext().getAuthentication().getName();
            String email = updates.get("email");
            String newTckn = updates.get("tckn");
            String username = updates.get("username");

            userService.updateDoctorProfile(tckn, email, newTckn, username);
            return ResponseEntity.ok(Map.of("message", "Profil başarıyla güncellendi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok(Map.of("message", "Doktor başarıyla silindi"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}