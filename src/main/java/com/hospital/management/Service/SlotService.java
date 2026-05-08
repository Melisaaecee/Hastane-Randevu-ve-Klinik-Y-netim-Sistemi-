package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.Entity.Slot;
import com.hospital.management.Entity.SlotStatus;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Exception.EntityNotFoundException;
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

    // GELECEK MÜSAİT SLOTLAR (Halka Açık Sorgu)
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

    // SLOT OLUŞTUR (IDOR Korumalı)
    @Transactional
    public Slot createSlot(Slot slot) {
        
        if (!SecurityUtil.isOwner(slot.getDoctor().getUser().getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Başka bir doktor adına çalışma saati (slot) oluşturamazsınız.");
        }

        LocalDateTime start = slot.getStartTime();
        LocalDateTime end = slot.getEndTime();

        // Geçmiş kontrolü - BadRequestException kullanımı
        if (start.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Geçmiş bir tarihe çalışma saati eklenemez.");
        }

        // Zaman kontrolü
        if (!start.isBefore(end)) {
            throw new BadRequestException("Başlangıç zamanı bitişten önce olmalıdır.");
        }

        // ÇAKIŞMA KONTROLÜ (Repository @Query kullanılıyor)
        List<Slot> conflicts = slotRepository.findConflictingSlots(slot.getDoctor().getId(), start, end);

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Bu saat aralığında doktorun zaten mevcut bir planı/slotu bulunuyor.");
        }

        slot.setStatus(SlotStatus.AVAILABLE);
        return slotRepository.save(slot);
    }

    // SLOT İPTAL (IDOR Korumalı)
    @Transactional
    public void cancelSlot(Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "İptal edilmek istenen slot bulunamadı (ID: " + slotId + ")"));

        // IDOR Kontrolü: Sadece slotun sahibi olan doktor veya admin iptal edebilir
        if (!SecurityUtil.isOwner(slot.getDoctor().getUser().getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Başkasına ait bir çalışma saatini iptal etme yetkiniz yok.");
        }

        // Randevu kontrolü
        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new BadRequestException(
                    "Üzerinde aktif randevu bulunan bir saati iptal edemezsiniz. Lütfen önce randevuyu iptal edin.");
        }

        slot.setStatus(SlotStatus.CANCELLED);
        slotRepository.save(slot);
    }
}