package com.hospital.management.Controller;

import com.hospital.management.Entity.Slot;
import com.hospital.management.Service.SlotService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus; // Eklendi
import org.springframework.http.ResponseEntity; // Eklendi
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
// @CrossOrigin(origins = "http://127.0.0.1:5500") // Eğer SecurityConfig'deki çalışmazsa burayı açarsın
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Slot>> getAvailable(@PathVariable Long doctorId) {
        return ResponseEntity.ok(slotService.getFutureAvailableSlots(doctorId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Slot> create(@RequestBody Slot slot) {
        // Service katmanında artık nesneyi doldurduğumuz için güvenle çağırıyoruz
        Slot createdSlot = slotService.createSlot(slot);
        return new ResponseEntity<>(createdSlot, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        slotService.cancelSlot(id);
        return ResponseEntity.noContent().build(); // 204 No Content döner
    }
}