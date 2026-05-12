package com.hospital.management.Controller;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.Entity.Penalty;
import com.hospital.management.Service.PenaltyService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Penalty>> getMyPenalties() {
        String tckn = SecurityUtil.getCurrentUserTckn();
        List<Penalty> penalties = penaltyService.getPenaltiesByTckn(tckn);
        return ResponseEntity.ok(penalties);
    }

    /// Süresi dolmuş cezaları listeleme (Admin görebilir).
    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Penalty> getExpired() {
        return penaltyService.getAllExpiredPenalties();
    }

    // Belirli bir hastanın tüm cezalarını getirir (Admin ve Hasta görebilir).
    @GetMapping("/tckn/{tckn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public List<Penalty> getByTckn(@PathVariable String tckn) {
        return penaltyService.getPenaltiesByTckn(tckn);
    }

    // Belirli bir hastanın aktif cezalarını getirir (Admin, Doktor ve Hasta
    // görebilir).
    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public List<Penalty> getActiveByPatient(@PathVariable Long patientId) {
        return penaltyService.getActivePenalties(patientId);
    }

    /// Süresi dolan cezaları manuel tetiklemek için (Opsiyonel).
    @PostMapping("/deactivate-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateExpired() {
        penaltyService.deactivateExpiredPenalties();
    }
}