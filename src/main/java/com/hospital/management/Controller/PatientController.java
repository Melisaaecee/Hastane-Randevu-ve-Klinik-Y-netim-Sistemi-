package com.hospital.management.Controller;

import com.hospital.management.Entity.Patient;
import com.hospital.management.Entity.Penalty;
import com.hospital.management.Service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/{id}")
    public Patient getById(@PathVariable Long id) {
        return patientService.getById(id);
    }

    @GetMapping("/user/{userId}")
    public Patient getByUserId(@PathVariable Long userId) {
        return patientService.getByUserId(userId);
    }

    @GetMapping("/tckn/{tckn}")
    public Patient getByTckn(@PathVariable String tckn) {
        return patientService.getByTckn(tckn);
    }

    @GetMapping
    public List<Patient> getAll() {
        return patientService.getAllPatients();
    }

    // CEZA
    @GetMapping("/{id}/penalties")
    public List<Penalty> getPenalties(@PathVariable Long id) {
        return patientService.getAllPenalties(id);
    }

    @GetMapping("/{id}/penalties/active")
    public List<Penalty> getActivePenalties(@PathVariable Long id) {
        return patientService.getActivePenalties(id);
    }

    @GetMapping("/{id}/penalty-status")
    public boolean hasPenalty(@PathVariable Long id) {
        return patientService.hasActivePenalty(id);
    }
}