package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.Entity.Doctor;
import com.hospital.management.Entity.Role;
import com.hospital.management.Entity.User;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.ClinicRepository;
import com.hospital.management.Repository.DoctorRepository;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== LİSTELEME METODLARI ====================

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAllWithDetails(); 
    }

    public List<Doctor> getAllDoctorsInClinic(Long clinicId) {
        List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);
        if (doctors.isEmpty()) {
            throw new EntityNotFoundException("Bu klinikte kayıtlı doktor bulunamadı.");
        }
        return doctors.stream()
                .sorted((d1, d2) -> d1.getUser().getFirstName().compareTo(d2.getUser().getFirstName()))
                .peek(doc -> {
                    String firstName = doc.getUser().getFirstName();
                    String specialization = doc.getSpecialization();

                    if (firstName != null && !firstName.startsWith("Dr.")) {
                        if (specialization != null && !specialization.isEmpty()
                                && !specialization.equals("Uzmanlık Belirtilmemiş")) {
                            // Uzmanlık varsa: ilk 3 harfini al + Dr.
                            String shortSpec = specialization.length() >= 3
                                    ? specialization.substring(0, 3).toUpperCase()
                                    : specialization.toUpperCase();
                            doc.getUser().setFirstName(shortSpec + ". Dr. " + firstName);
                        } else {
                            // Uzmanlık yoksa: sadece Dr.
                            doc.getUser().setFirstName("Dr. " + firstName);
                        }
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

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

    // ==================== DOKTOR OLUŞTURMA (Admin için) ====================

    @Transactional
    public Map<String, Object> createDoctorWithUser(
            String firstName,
            String lastName,
            String specialization,
            Long clinicId) {

        // 1. Geçici bilgiler oluştur
        String username = generateUsername(firstName, lastName);
        String plainPassword = generateRandomPassword();
        String tempEmail = generateTempEmail(firstName, lastName);
        String tempTckn = generateRandomTckn();

        // 2. User oluştur
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTckn(tempTckn);
        user.setEmail(tempEmail);
        user.setRole(Role.DOCTOR);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);

        User savedUser = userRepository.save(user);

        // 3. Clinic bul
        var clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new RuntimeException("Klinik bulunamadı: " + clinicId));

        // 4. Doctor oluştur
        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setSpecialization(specialization != null ? specialization : "Uzmanlık Belirtilmemiş");
        doctor.setClinic(clinic);

        Doctor savedDoctor = doctorRepository.save(doctor);

        // 5. Response hazırla
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedDoctor.getId());
        response.put("firstName", savedUser.getFirstName());
        response.put("lastName", savedUser.getLastName());
        response.put("specialty", savedDoctor.getSpecialization());
        response.put("clinicName", clinic.getName());
        response.put("username", username);
        response.put("temporaryPassword", plainPassword);
        response.put("temporaryEmail", tempEmail);
        response.put("temporaryTckn", tempTckn);
        response.put("message", "Doktor başarıyla eklendi!");

        return response;
    }

    // ==================== GÜNCELLEME METODLARI ====================

    @Transactional
    public Doctor saveOrUpdateDoctor(Doctor doctor) {
        if (doctor.getUser() != null && !SecurityUtil.isOwner(doctor.getUser().getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu doktor profilini güncelleme yetkiniz yok.");
        }
        return doctorRepository.save(doctor);
    }

    // ==================== SİLME METODLARI ====================

    @Transactional
    public void deleteDoctor(Long id) {
        // Önce doktoru bul
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doktor bulunamadı (ID: " + id + ")"));

        // User ID'sini al
        Long userId = doctor.getUser().getId();

        // DOKTORU SİL
        doctorRepository.delete(doctor);
        doctorRepository.flush(); // Hemen veritabanına yaz

        // KULLANICIYI DA SİL
        userRepository.deleteById(userId);
        userRepository.flush();

        System.out.println("✅ Doktor ve kullanıcı silindi - Doctor ID: " + id + ", User ID: " + userId);
    }

    // ==================== PRIVATE HELPER METODLAR ====================

    private String generateUsername(String firstName, String lastName) {
        String normalizedFirst = normalizeTurkish(firstName);
        String normalizedLast = normalizeTurkish(lastName);
        String base = (normalizedFirst + "." + normalizedLast).toLowerCase();

        String username = base;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + counter;
            counter++;
        }
        return username;
    }

    private String generateTempEmail(String firstName, String lastName) {
        String normalizedFirst = normalizeTurkish(firstName);
        String normalizedLast = normalizeTurkish(lastName);
        String base = (normalizedFirst + "." + normalizedLast).toLowerCase();
        String email = base + "@hastane.com";

        int counter = 1;
        while (userRepository.existsByEmail(email)) {
            email = base + counter + "@hastane.com";
            counter++;
        }
        return email;
    }

    private String generateRandomTckn() {
        Random random = new Random();
        int[] digits = new int[11];

        digits[0] = 1 + random.nextInt(9);
        for (int i = 1; i < 9; i++) {
            digits[i] = random.nextInt(10);
        }

        int sumFirst9 = 0;
        for (int i = 0; i < 9; i++) {
            sumFirst9 += digits[i];
        }
        digits[9] = sumFirst9 % 10;

        int sumFirst10Special = 0;
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                sumFirst10Special += digits[i];
            }
        }
        int total = sumFirst10Special * 7;
        for (int i = 1; i < 9; i += 2) {
            total -= digits[i];
        }
        digits[10] = total % 10;

        StringBuilder tckn = new StringBuilder();
        for (int digit : digits) {
            tckn.append(digit);
        }
        return tckn.toString();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private String normalizeTurkish(String text) {
        if (text == null)
            return "";
        return text.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("ğ", "g")
                .replaceAll("ü", "u")
                .replaceAll("ş", "s")
                .replaceAll("ı", "i")
                .replaceAll("ö", "o")
                .replaceAll("ç", "c");
    }
}