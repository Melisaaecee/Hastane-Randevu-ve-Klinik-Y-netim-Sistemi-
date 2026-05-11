package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.Entity.Doctor; // Eklendi
import com.hospital.management.Entity.Slot;
import com.hospital.management.Entity.SlotStatus;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.DoctorRepository; // Eklendi
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
    private final DoctorRepository doctorRepository; // Veritabanından çekmek için eklendi

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

    // Doktorun slotlarını getir
    public List<Slot> getSlotsByDoctorId(Long doctorId) {
        return slotRepository.findByDoctorIdWithDetails(doctorId); // ✅ Fetch join
    }

@Transactional
public Slot createSlot(Slot slot) {
    // 1. Veri kontrolü
    if (slot.getDoctor() == null || slot.getDoctor().getId() == null) {
        throw new BadRequestException("Doktor ID bilgisi eksik.");
    }

    // 2. Veritabanından doktoru bul (Kendi ID'si ile)
    Doctor doctor = doctorRepository.findById(slot.getDoctor().getId())
            .orElseThrow(() -> new EntityNotFoundException("Doktor bulunamadı."));

    // 3. Eksik bilgileri tamamla
    slot.setDoctor(doctor);
    slot.setClinic(doctor.getClinic()); 
    slot.setStatus(SlotStatus.AVAILABLE);

    // 4. Güvenlik ve Tarih Kontrolleri (Senin mevcut kodun)
    if (!SecurityUtil.isOwner(doctor.getUser().getId()) && !SecurityUtil.isAdmin()) {
        throw new AccessDeniedException("Yetkisiz işlem!");
    }

    if (slot.getStartTime().isBefore(LocalDateTime.now())) {
        throw new BadRequestException("Geçmiş tarihe slot eklenemez.");
    }

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

    public List<Slot> getAllSlots() {
        return slotRepository.findAll();
    }
}