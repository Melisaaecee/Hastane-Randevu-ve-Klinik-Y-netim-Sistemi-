package com.hospital.management.Service;

import com.hospital.management.Entity.Patient;
import com.hospital.management.Entity.Penalty;
import com.hospital.management.Repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PenaltyService penaltyService;

    // 🔥 ID ile hasta getir
    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı: " + id));
    }

    // 🔥 userId ile hasta getir
    public Patient getByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcıya bağlı hasta bulunamadı"));
    }

    // 🔥 TCKN ile hasta getir
    public Patient getByTckn(String tckn) {
        return patientRepository.findByUserTckn(tckn)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı: " + tckn));
    }

    // 🔥 hasta kaydet / güncelle
    @Transactional
    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    // 🔥 tüm cezalar
    public List<Penalty> getAllPenalties(Long patientId) {
        return penaltyService.getPatientPenalties(patientId);
    }

    // 🔥 aktif ceza var mı
    public boolean hasActivePenalty(Long patientId) {
        return penaltyService.hasActivePenalty(patientId);
    }

    // 🔥 aktif cezalar
    public List<Penalty> getActivePenalties(Long patientId) {
        return penaltyService.getActivePenalties(patientId);
    }

    // Tüm hastaları listelemek için (Admin Paneli İçin)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}