package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.Entity.Appointment;
import com.hospital.management.Entity.Penalty;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PenaltyService {

    private final PenaltyRepository penaltyRepository;

    // HASTANIN AKTİF CEZASI VAR MI?
    public boolean hasActivePenalty(Long patientId) {
        return penaltyRepository.existsByPatientIdAndActiveTrue(patientId);
    }

    // HASTANIN TÜM CEZALARI
    public List<Penalty> getPatientPenalties(Long patientId) {
        return penaltyRepository.findByPatientId(patientId);
    }

    // HASTANIN AKTİF CEZALARI
    public List<Penalty> getActivePenalties(Long patientId) {
        return penaltyRepository.findByPatientIdAndActiveTrue(patientId);
    }

    // HASTANIN GEÇMİŞ CEZALARI
    public List<Penalty> getPastPenalties(Long patientId) {
        return penaltyRepository.findByPatientIdAndPenaltyEndDateBefore(
                patientId,
                LocalDateTime.now());
    }

    // SİSTEMDEKİ TÜM SÜRESİ DOLMUŞ CEZALAR (ADMIN İÇİN)
    public List<Penalty> getAllExpiredPenalties() {
        return penaltyRepository.findByPenaltyEndDateBefore(LocalDateTime.now());
    }

    // TCKN İLE CEZALARI GETİR
    public List<Penalty> getPenaltiesByTckn(String tckn) {

        if (!SecurityUtil.isAdmin() && !SecurityUtil.getCurrentUserTckn().equals(tckn)) {
            throw new AccessDeniedException("Sadece kendi ceza bilgilerinize erişebilirsiniz.");
        }
        return penaltyRepository.findByPatientUserTckn(tckn);
    }

    // CEZA OLUŞTUR
    @Transactional
    public Penalty createPenaltyFromAppointment(Appointment appointment) {

        if (hasActivePenalty(appointment.getPatient().getId())) {
            throw new BadRequestException("Bu hastanın zaten aktif bir cezası bulunmaktadır.");
        }

        Penalty penalty = new Penalty();
        penalty.setPatient(appointment.getPatient());
        penalty.setAppointment(appointment);
        penalty.setPenaltyStartDate(LocalDateTime.now());
        penalty.setPenaltyEndDate(LocalDateTime.now().plusDays(7));
        penalty.setActive(true);

        return penaltyRepository.save(penalty);
    }

    // SÜRESİ DOLMUŞ CEZALARI PASİFLEŞTİR
    @Transactional
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