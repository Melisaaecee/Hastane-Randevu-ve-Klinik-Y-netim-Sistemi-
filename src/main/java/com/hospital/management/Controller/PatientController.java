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
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR') or @patientService.isPatientOwner(#id, principal.username)")
    public Patient getById(@PathVariable Long id) {
        return patientService.getById(id);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR') or #userId == principal.user.id")
    public Patient getByUserId(@PathVariable Long userId) {
        return patientService.getByUserId(userId);
    }

    @GetMapping("/tckn/{tckn}")
    @PreAuthorize("hasRole('ADMIN') or principal.user.tckn == #tckn")
    public Patient getByTckn(@PathVariable String tckn) {
        return patientService.getByTckn(tckn);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Patient> getAll() {
        return patientService.getAllPatients();
    }

    // CEZA
    @GetMapping("/{id}/penalties")
    @PreAuthorize("hasRole('ADMIN') or @patientService.isPatientOwner(#id, principal.username)")
    public List<Penalty> getPenalties(@PathVariable Long id) {
        return patientService.getAllPenalties(id);
    }

    @GetMapping("/{id}/penalties/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR') or @patientService.isPatientOwner(#id, principal.username)")
    public List<Penalty> getActivePenalties(@PathVariable Long id) {
        return patientService.getActivePenalties(id);
    }

    @GetMapping("/{id}/penalty-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public boolean hasPenalty(@PathVariable Long id) {
        return patientService.hasActivePenalty(id);
    }
}