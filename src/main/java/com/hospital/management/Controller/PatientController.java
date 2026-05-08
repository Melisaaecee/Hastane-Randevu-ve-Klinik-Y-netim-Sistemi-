package com.hospital.management.Controller;

import com.hospital.management.Entity.Patient;
import com.hospital.management.Entity.Penalty;
import com.hospital.management.Service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;

    // --- TEMEL HASTA BİLGİLERİ ---

    // ID ile hasta sorgular.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public Patient getById(@PathVariable Long id) {
        return patientService.getById(id);
    }

    // TCKN ile hasta sorgular.
    @GetMapping("/tckn/{tckn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Patient getByTckn(@PathVariable String tckn) {
        return patientService.getByTckn(tckn);
    }

    // User ID ile hasta sorgular.
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Patient getByUserId(@PathVariable Long userId) {
        return patientService.getByUserId(userId);
    }

    // --- CEZA (PENALTY) İŞLEMLERİ ---

    // Hastanın aktif cezalarını listeler.
    @GetMapping("/{id}/penalties/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public List<Penalty> getActivePenalties(@PathVariable Long id) {
        return patientService.getActivePenalties(id);
    }

    // Hastanın tüm cezalarını listeler (aktif ve geçmiş).
    @GetMapping("/{id}/penalties/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public List<Penalty> getAllPenalties(@PathVariable Long id) {
        return patientService.getAllPenalties(id);
    }

    // Hastanın aktif cezası olup olmadığını kontrol eder.
    @GetMapping("/{id}/has-active-penalty")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public boolean checkActivePenalty(@PathVariable Long id) {
        return patientService.hasActivePenalty(id);
    }

    // --- YÖNETİMSEL VE KAYIT İŞLEMLERİ ---

    // Tüm hastaları listele (Admin görebilir).
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Patient> getAll() {
        return patientService.getAllPatients();
    }

    // Hasta bilgilerini kaydetme/güncelleme işlemi. (Admin ve ilgili hasta kendisi
    // yapabilir)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Patient save(@RequestBody Patient patient) {
        return patientService.save(patient);
    }
}