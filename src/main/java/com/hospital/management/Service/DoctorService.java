package com.hospital.management.Service;

import com.hospital.management.Entity.Doctor;
import com.hospital.management.Repository.DoctorRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor // Constructor Injection için (Lombok)
public class DoctorService {

    private final DoctorRepository doctorRepository;

    // 1. Tüm doktorları listele
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

  /** 2. belirli bir klinikteki doktorları getirir, alfabetik sıralar ve isimlerine unvan ekler.
     * SADECE ADMIN GÖREBİLİR:
     * Kliniğe göre doktorları getirir, alfabetik sıralar ve isimlerine unvan ekler.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<Doctor> getAllDoctorsInClinicForAdmin(Long clinicId) {
        // 1. Veritabanından ham listeyi al
        List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);

        if (doctors.isEmpty()) {
            throw new RuntimeException("Bu klinikte kayıtlı doktor bulunamadı.");
        }

        // 2. Stream API ile işlemleri yap
        return doctors.stream()
                // Alfabetik sıralama (A'dan Z'ye - İsim bazlı)
                .sorted(Comparator.comparing(doc -> doc.getUser().getFirstName()))
                // İsimlerin başına unvan ekleme ve listeyi hazırlama
                .peek(doc -> {
                    String originalFirstName = doc.getUser().getFirstName();
                    if (!originalFirstName.startsWith("Uzm. Dr.")) {
                        doc.getUser().setFirstName("Uzm. Dr. " + originalFirstName);
                    }
                })
                .collect(Collectors.toList());
    }

    // 4. Uzmanlık alanına göre filtrele
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    // 5. Giriş yapan kullanıcının "Doktor" profilini getir
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Bu kullanıcıya ait bir doktor profili bulunamadı!"));
    }

    // 6. Klinik içinde isimle arama yap
    public List<Doctor> searchDoctorInClinic(Long clinicId, String name) {
        return doctorRepository.findByClinicIdAndUserFirstNameContainingIgnoreCase(clinicId, name);
    }

    // 7. Yeni doktor ekle veya güncelle
    public Doctor saveOrUpdateDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    // 8. Doktor sil
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new RuntimeException("Silinmek istenen doktor bulunamadı!");
        }
        doctorRepository.deleteById(id);
    }
}
