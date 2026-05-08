package com.hospital.management.Controller;

import com.hospital.management.Entity.Slot;
import com.hospital.management.Service.SlotService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/doctor/{doctorId}")
    public List<Slot> getAvailable(@PathVariable Long doctorId) {
        return slotService.getFutureAvailableSlots(doctorId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Slot create(@RequestBody Slot slot) {
        return slotService.createSlot(slot);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public void cancel(@PathVariable Long id) {
        slotService.cancelSlot(id);
    }
}