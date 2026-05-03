package com.hospital.management.Controller;

import com.hospital.management.Entity.Penalty;
import com.hospital.management.Service.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;

    @GetMapping("/expired")
    public List<Penalty> getExpired() {
        return penaltyService.getAllExpiredPenalties();
    }

    @GetMapping("/tckn/{tckn}")
    public List<Penalty> getByTckn(@PathVariable String tckn) {
        return penaltyService.getPenaltiesByTckn(tckn);
    }
}