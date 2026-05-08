package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.Entity.Doctor;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    // Tüm doktorları listele (Halka açık)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // Kliniğe göre doktorları getirir, alfabetik sıralar ve unvan ekler.
    public List<Doctor> getAllDoctorsInClinic(Long clinicId) {
        List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);

        if (doctors.isEmpty()) {
            throw new EntityNotFoundException("Bu klinikte kayıtlı doktor bulunamadı.");
        }

        return doctors.stream()
                .sorted(Comparator.comparing(doc -> doc.getUser().getFirstName()))
                .peek(doc -> {
                    String firstName = doc.getUser().getFirstName();
                    if (firstName != null && !firstName.startsWith("Uzm. Dr.")) {
                        doc.getUser().setFirstName("Uzm. Dr. " + firstName);
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    //  Sadece doktor kendi profilini veya Admin herkesi görebilir
    public Doctor getDoctorByUserId(Long userId) {
        if (!SecurityUtil.isOwner(userId) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu doktor profiline erişim yetkiniz yok.");
        }
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Bu kullanıcıya ait bir doktor profili bulunamadı!"));
    }

    public List<Doctor> searchDoctorInClinic(Long clinicId, String name) {
        return doctorRepository.findByClinicIdAndUserFirstNameContainingIgnoreCase(clinicId, name);
    }

    //  Sadece doktor kendi bilgilerini güncelleyebilir
    @Transactional
    public Doctor saveOrUpdateDoctor(Doctor doctor) {
        if (doctor.getUser() != null && !SecurityUtil.isOwner(doctor.getUser().getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu doktor profilini güncelleme yetkiniz yok.");
        }
        return doctorRepository.save(doctor);
    }

    // Sadece Admin Silebilir
    @Transactional
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new EntityNotFoundException("Silinmek istenen doktor bulunamadı (ID: " + id + ")");
        }
        doctorRepository.deleteById(id);
    }
}