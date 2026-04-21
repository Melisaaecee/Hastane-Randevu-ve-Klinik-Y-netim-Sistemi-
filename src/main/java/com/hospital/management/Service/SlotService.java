package com.hospital.management.Service;

import com.hospital.management.Entity.Slot;
import com.hospital.management.Entity.SlotStatus;
import com.hospital.management.Repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;

    // 🔥 GELECEK MÜSAİT SLOTLAR
    public List<Slot> getFutureAvailableSlots(Long doctorId) {
        return slotRepository
                .findByDoctorIdAndStatusAndStartTimeAfter(
                        doctorId,
                        SlotStatus.AVAILABLE,
                        LocalDateTime.now())
                .stream()
                .sorted(Comparator.comparing(Slot::getStartTime))
                .collect(Collectors.toList());
    }

    // 🔥 SLOT OLUŞTUR (CLEAN VERSION)
    @Transactional
    public Slot createSlot(Slot slot) {

        Long doctorId = slot.getDoctor().getId();
        LocalDateTime start = slot.getStartTime();
        LocalDateTime end = slot.getEndTime();

        // ❌ geçmiş kontrolü
        if (start.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Geçmiş tarihe slot oluşturulamaz");
        }

        // ❌ zaman kontrolü
        if (!start.isBefore(end)) {
            throw new RuntimeException("Başlangıç zamanı bitişten önce olmalı");
        }

        // 🔥 TEK GERÇEK ÇAKIŞMA KONTROLÜ (DB LEVEL)
        List<Slot> conflicts = slotRepository.findConflictingSlots(doctorId, start, end);

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Bu zaman aralığında zaten bir slot var");
        }

        slot.setStatus(SlotStatus.AVAILABLE);

        return slotRepository.save(slot);
    }

    // 🔥 SLOT İPTAL
    @Transactional
    public void cancelSlot(Long slotId) {

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot bulunamadı"));

        // randevu alınmış slot direkt iptal edilmez
        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new RuntimeException("Randevu alınmış slot iptal edilemez");
        }

        slot.setStatus(SlotStatus.CANCELLED);
        slotRepository.save(slot);
    }
}