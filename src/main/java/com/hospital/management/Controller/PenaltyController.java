package com.hospital.management.Controller;

import com.hospital.management.Entity.Penalty;
import com.hospital.management.Service.PenaltyService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;

    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Penalty> getExpired() {
        return penaltyService.getAllExpiredPenalties();
    }

    @GetMapping("/tckn/{tckn}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PATIENT') and principal.user.tckn == #tckn)")
    public List<Penalty> getByTckn(@PathVariable String tckn) {
        return penaltyService.getPenaltiesByTckn(tckn);
    }
}