package com.hospital.management.Service;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Entity.Penalty;
import com.hospital.management.Repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PenaltyService {

    private final PenaltyRepository penaltyRepository;

    // 🔥 1. Aktif ceza var mı?
    public boolean hasActivePenalty(Long patientId) {
        return penaltyRepository.existsByPatientIdAndActiveTrue(patientId);
    }

    // 🔥 2. Tüm cezalar
    public List<Penalty> getPatientPenalties(Long patientId) {
        return penaltyRepository.findByPatientId(patientId);
    }

    // 🔥 3. Aktif cezalar
    public List<Penalty> getActivePenalties(Long patientId) {
        return penaltyRepository.findByPatientIdAndActiveTrue(patientId);
    }

    // 🔥 4. Geçmiş cezalar (FIXED)
    public List<Penalty> getPastPenalties(Long patientId) {
        return penaltyRepository.findByPatientIdAndPenaltyEndDateBefore(
                patientId,
                LocalDateTime.now());
    }

    // 🔥 5. Expired cezalar (FIXED - SENİN HATAN BURADAYDI)
    public List<Penalty> getAllExpiredPenalties() {
        return penaltyRepository.findByPenaltyEndDateBefore(LocalDateTime.now());
    }

    // 🔥 6. TCKN ile sorgu
    public List<Penalty> getPenaltiesByTckn(String tckn) {
        return penaltyRepository.findByPatientUserTckn(tckn);
    }

    // 🔥 7. Ceza oluştur
    // 🔥 Ceza oluştur (tek sorumluluk)
    public Penalty createPenaltyFromAppointment(Appointment appointment) {

        // Aynı hastada aktif ceza varsa tekrar üretme
        if (hasActivePenalty(appointment.getPatient().getId())) {
            throw new RuntimeException("Aktif ceza zaten var");
        }


        Penalty penalty = new Penalty();
        penalty.setPatient(appointment.getPatient());
        penalty.setAppointment(appointment);
        penalty.setPenaltyStartDate(LocalDateTime.now());
        penalty.setPenaltyEndDate(LocalDateTime.now().plusDays(7));
        penalty.setActive(true);

        return penaltyRepository.save(penalty);
    }

    // 🔥 8. Süresi dolanları kapat (FIXED)
    public void deactivateExpiredPenalties() {

        List<Penalty> expiredPenalties = penaltyRepository.findByPenaltyEndDateBefore(LocalDateTime.now());

        for (Penalty penalty : expiredPenalties) {
            if (penalty.isActive()) {
                penalty.setActive(false);
            }
        }

        penaltyRepository.saveAll(expiredPenalties);
    }
}