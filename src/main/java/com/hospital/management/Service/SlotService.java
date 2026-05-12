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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    // GELECEK MÜSAİT SLOTLAR
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
        return slotRepository.findByDoctorIdWithDetails(doctorId);
    }



    public List<Slot> getSlotsByDoctorAndDate(Long doctorId, String dateStr) {
    // 1. Frontend'den gelen "2026-05-15" gibi String'i LocalDate'e çeviriyoruz
    LocalDate date = LocalDate.parse(dateStr);
    
    // 2. O günün başlangıcını (00:00:00) ve bitişini (23:59:59) belirliyoruz
    LocalDateTime startOfDay = date.atStartOfDay(); 
    LocalDateTime endOfDay = date.atTime(LocalTime.MAX); 

    // 3. Repository'deki yeni metodunu kullanarak bu iki zaman arasındaki AVAILABLE slotları çekiyoruz
    return slotRepository.findByDoctorIdAndStartTimeBetweenAndStatus(
            doctorId, 
            startOfDay, 
            endOfDay, 
            SlotStatus.AVAILABLE
    );
}

    @Transactional
public Slot createSlot(Slot slot) {
    // 1. Veri kontrolü
    if (slot.getDoctor() == null || slot.getDoctor().getId() == null) {
        throw new BadRequestException("Doktor ID bilgisi eksik.");
    }

    // 2. Veritabanından doktoru bul
    Doctor doctor = doctorRepository.findById(slot.getDoctor().getId())
            .orElseThrow(() -> new EntityNotFoundException("Doktor bulunamadı."));

    // 3. Eksik bilgileri tamamla
    slot.setDoctor(doctor);
    slot.setClinic(doctor.getClinic());
    slot.setStatus(SlotStatus.AVAILABLE);

    // 4. Güvenlik ve Mantıksal Kontroller
    if (!SecurityUtil.isOwner(doctor.getUser().getId()) && !SecurityUtil.isAdmin()) {
        throw new AccessDeniedException("Yetkisiz işlem!");
    }

    // --- YENİ KONTROLLER BAŞLANGIÇ ---

    // KURAL 1: Başlangıç ve Bitiş saati aynı olamaz
    if (slot.getStartTime().isEqual(slot.getEndTime())) {
        throw new BadRequestException("❌ Slotun başlangıç ve bitiş saati aynı olamaz.");
    }

    // KURAL 2: Bitiş saati başlangıçtan önce olamaz (Mantık hatası engelleme)
    if (slot.getEndTime().isBefore(slot.getStartTime())) {
        throw new BadRequestException("❌ Bitiş saati başlangıç saatinden önce olamaz.");
    }

    // KURAL 3: Slotun başlangıç ve bitişi aynı gün içerisinde olmalı
    if (!slot.getStartTime().toLocalDate().isEqual(slot.getEndTime().toLocalDate())) {
        throw new BadRequestException("❌ Bir slot sadece tek bir gün içinde tanımlanabilir. Gün aşırı slot oluşturulamaz.");
    }

    // KURAL 4: Geçmiş tarihe slot eklenemez
    if (slot.getStartTime().isBefore(LocalDateTime.now())) {
        throw new BadRequestException("❌ Geçmiş tarihe slot eklenemez.");
    }

        // ÇAKIŞMA KONTROLÜ 
        checkSlotConflict(doctor.getId(), slot.getStartTime(), slot.getEndTime());

        return slotRepository.save(slot);
    }

    //  Slot çakışmasını kontrol et
    private void checkSlotConflict(Long doctorId, LocalDateTime newStart, LocalDateTime newEnd) {
        // Doktorun mevcut tüm slotlarını getir
        List<Slot> existingSlots = slotRepository.findByDoctorIdWithDetails(doctorId);

        for (Slot existing : existingSlots) {
            LocalDateTime existingStart = existing.getStartTime();
            LocalDateTime existingEnd = existing.getEndTime();


            boolean isOverlap = (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart));

            if (isOverlap) {
                throw new BadRequestException(
                        String.format("❌ Slot çakışması! Bu saat aralığında zaten slot var.\n" +
                                "Mevcut slot: %s - %s\n" +
                                "Eklemek istediğiniz: %s - %s",
                                formatTime(existingStart), formatTime(existingEnd),
                                formatTime(newStart), formatTime(newEnd)));
            }
        }
    }

    // Yardımcı metod: Saat formatı
    private String formatTime(LocalDateTime time) {
        return time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
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