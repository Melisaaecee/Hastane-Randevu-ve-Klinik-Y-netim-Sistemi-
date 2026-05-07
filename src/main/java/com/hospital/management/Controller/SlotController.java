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

    //  MÜSAİT SLOTLAR
    @GetMapping("/doctor/{doctorId}")
    public List<Slot> getAvailable(@PathVariable Long doctorId) {
        return slotService.getFutureAvailableSlots(doctorId);
    }

    //  SLOT OLUŞTUR
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and #slot.doctor.user.username == principal.username)")
    public Slot create(@RequestBody Slot slot) {
        return slotService.createSlot(slot);
    }

    //  SLOT İPTAL
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @slotService.isSlotOwner(#id, principal.username)")
    public void cancel(@PathVariable Long id) {
        slotService.cancelSlot(id);
    }
}