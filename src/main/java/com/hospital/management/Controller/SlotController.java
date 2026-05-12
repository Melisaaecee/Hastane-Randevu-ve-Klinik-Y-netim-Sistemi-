package com.hospital.management.Controller;

import com.hospital.management.Entity.Slot;
import com.hospital.management.Service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; 
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor

public class SlotController {

    private final SlotService slotService;

   @GetMapping("/doctor/{doctorId}")
public ResponseEntity<List<Slot>> getAvailable(
        @PathVariable Long doctorId,
        @RequestParam(required = false) String date) { // 'date' parametresini opsiyonel yaptık

    // Eğer URL'de ?date=2026-05-15 gibi bir tarih varsa
    if (date != null && !date.isEmpty()) {
        return ResponseEntity.ok(slotService.getSlotsByDoctorAndDate(doctorId, date));
    }
    
    // Eğer tarih seçilmemişse, mevcut sistemin gibi gelecekteki tüm müsait slotları döner
    return ResponseEntity.ok(slotService.getFutureAvailableSlots(doctorId));
}

    @GetMapping
    public ResponseEntity<List<Slot>> getAllSlots() {
        return ResponseEntity.ok(slotService.getAllSlots());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Slot> create(@RequestBody Slot slot) {
        Slot createdSlot = slotService.createSlot(slot);
        return new ResponseEntity<>(createdSlot, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        slotService.cancelSlot(id);
        return ResponseEntity.noContent().build();
    }
}