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

    /**
     * Tüm doktorları listeler.
     * GET http://localhost:8080/api/doctors
     */
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    /**
     * Belirli bir klinikteki doktorları alfabetik ve unvanlı getirir (ADMIN Özel).
     * GET http://localhost:8080/api/doctors/clinic/{clinicId}/admin
     */
    @GetMapping("/clinic/{clinicId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Doctor>> getAllDoctorsInClinicForAdmin(@PathVariable Long clinicId) {
        return ResponseEntity.ok(doctorService.getAllDoctorsInClinicForAdmin(clinicId));
    }

    /**
     * Uzmanlık alanına göre filtreleme yapar.
     * GET http://localhost:8080/api/doctors/specialization?name=Kardiyoloji
     */
    @GetMapping("/specialization")
    public ResponseEntity<List<Doctor>> getBySpecialization(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(name));
    }

    /**
     * Kullanıcı ID'sine göre doktor profilini getirir.
     * GET http://localhost:8080/api/doctors/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Doctor> getDoctorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(doctorService.getDoctorByUserId(userId));
    }

    /**
     * Klinik içinde isimle doktor arama.
     * GET http://localhost:8080/api/doctors/search?clinicId=1&name=Ahmet
     */
    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctor(
            @RequestParam Long clinicId, 
            @RequestParam String name) {
        return ResponseEntity.ok(doctorService.searchDoctorInClinic(clinicId, name));
    }

    /**
     * Yeni doktor ekleme veya güncelleme.
     * POST http://localhost:8080/api/doctors
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> saveOrUpdate(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.saveOrUpdateDoctor(doctor));
    }

    /**
     * Doktor kaydını siler.
     * DELETE http://localhost:8080/api/doctors/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok("Doktor kaydı başarıyla silindi.");
    }
}