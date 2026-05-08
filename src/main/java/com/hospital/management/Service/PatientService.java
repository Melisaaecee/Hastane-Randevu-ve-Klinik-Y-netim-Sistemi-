package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.Entity.*;
import com.hospital.management.Exception.*;
import com.hospital.management.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PenaltyService penaltyService;

    // ID ile hasta getir (IDOR Korumalı)
    public Patient getById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hasta bulunamadı: " + id));

        // Sahiplik kontrolü: Hastanın bağlı olduğu User ID, mevcut User ID ile aynı mı?
        if (!SecurityUtil.isOwner(patient.getUser().getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu hasta bilgilerine erişim yetkiniz yok.");
        }
        return patient;
    }

    // userId ile hasta getir (IDOR Korumalı)
    public Patient getByUserId(Long userId) {
        if (!SecurityUtil.isOwner(userId) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Yetkisiz erişim denemesi.");
        }
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcıya bağlı hasta bulunamadı"));
    }

    // TCKN ile hasta getir (IDOR Korumalı)
    public Patient getByTckn(String tckn) {
        Patient patient = patientRepository.findByUserTckn(tckn)
                .orElseThrow(() -> new EntityNotFoundException("Hasta bulunamadı: " + tckn));

        // TCKN hassas veri olduğu için sahiplik kontrolü şart
        if (!SecurityUtil.isOwner(patient.getUser().getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu TCKN bilgilerini görmeye yetkiniz yok.");
        }
        return patient;
    }

    @Transactional
    public Patient save(Patient patient) {
        
        if (patient.getUser() != null && !SecurityUtil.isOwner(patient.getUser().getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu profili kaydetme yetkiniz yok.");
        }
        return patientRepository.save(patient);
    }

    // Tüm cezaları listele (IDOR Korumalı)
    public List<Penalty> getAllPenalties(Long patientId) {
        validatePatientOwnership(patientId);
        return penaltyService.getPatientPenalties(patientId);
    }

    // Aktif ceza var mı kontrolü 
    public boolean hasActivePenalty(Long patientId) {
        validatePatientOwnership(patientId);
        return penaltyService.hasActivePenalty(patientId);
    }

    // Aktif cezaları getir
    public List<Penalty> getActivePenalties(Long patientId) {
        validatePatientOwnership(patientId);
        return penaltyService.getActivePenalties(patientId);
    }

    // Admin Paneli İçin
    @PreAuthorize("hasRole('ADMIN')")
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    
    private void validatePatientOwnership(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Hasta bulunamadı."));

        if (!SecurityUtil.isOwner(patient.getUser().getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu verilere erişim yetkiniz bulunmamaktadır.");
        }
    }
}