package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.DTO.DoctorProfileRequest;
import com.hospital.management.Entity.Doctor;
import com.hospital.management.Entity.Role;
import com.hospital.management.Entity.User;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.ClinicRepository;
import com.hospital.management.Repository.DoctorRepository;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Doktor listesini normalize eder - SADECE SIRALAMA YAPAR, İSİM DEĞİŞTİRMEZ
     */
    private List<Doctor> normalizeDoctors(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return doctors;
        }

        // Sadece isme göre sırala, isimleri DEĞİŞTİRME
        return doctors.stream()
                .sorted((d1, d2) -> {
                    String name1 = d1.getUser() != null ? d1.getUser().getFirstName() : "";
                    String name2 = d2.getUser() != null ? d2.getUser().getFirstName() : "";
                    return name1.compareTo(name2);
                })
                .collect(Collectors.toList());
    }

    /**
     * Tek bir doktoru normalize eder - DEĞİŞİKLİK YAPMAZ
     */
    private Doctor normalizeDoctor(Doctor doctor) {
        return doctor; // Hiçbir değişiklik yapma
    }

    // Ünvan doğrulama - SADECE DOĞRULAMA YAPAR, İSİM DEĞİŞTİRMEZ
    private String validateAndFixTitle(String title) {
        if (title == null || title.isEmpty()) {
            return null;
        }

        String lowerTitle = title.toLowerCase().trim();

        // Geçerli ünvanlar ve doğru yazımları
        Map<String, String> validTitles = new HashMap<>();
        validTitles.put("prof", "Prof");
        validTitles.put("prof.", "Prof");
        validTitles.put("profesör", "Prof");
        validTitles.put("professor", "Prof");
        validTitles.put("doç", "Doç");
        validTitles.put("doç.", "Doç");
        validTitles.put("doçent", "Doç");
        validTitles.put("uzm", "Uzm");
        validTitles.put("uzm.", "Uzm");
        validTitles.put("uzman", "Uzm");
        validTitles.put("op", "Op");
        validTitles.put("op.", "Op");
        validTitles.put("operatör", "Op");
        validTitles.put("dr", "Dr");
        validTitles.put("dr.", "Dr");

        // Hatalı yazımları kontrol et
        if (lowerTitle.equals("porf") || lowerTitle.equals("porf.")) {
            throw new IllegalArgumentException("❌ Geçersiz ünvan: 'porf' doğru yazımı 'Prof' olmalıdır.");
        }
        if (lowerTitle.equals("doc") || lowerTitle.equals("doc.")) {
            throw new IllegalArgumentException("❌ Geçersiz ünvan: 'doc' doğru yazımı 'Doç' olmalıdır.");
        }

        for (Map.Entry<String, String> entry : validTitles.entrySet()) {
            if (lowerTitle.equals(entry.getKey()) || lowerTitle.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException("❌ Geçersiz ünvan! Geçerli ünvanlar: Prof, Doç, Uzm, Op, Dr");
    }

    // ==================== LİSTELEME METODLARI ====================

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAllWithDetails();
        return normalizeDoctors(doctors);
    }

    public List<Doctor> getAllDoctorsInClinic(Long clinicId) {
        List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);
        if (doctors.isEmpty()) {
            throw new EntityNotFoundException("Bu klinikte kayıtlı doktor bulunamadı.");
        }
        return normalizeDoctors(doctors);
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        List<Doctor> doctors = doctorRepository.findBySpecialization(specialization);
        return normalizeDoctors(doctors);
    }

    public Doctor getDoctorByUserId(Long userId) {
        if (!SecurityUtil.isOwner(userId) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu doktor profiline erişim yetkiniz yok.");
        }
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Bu kullanıcıya ait bir doktor profili bulunamadı!"));
        return normalizeDoctor(doctor);
    }

    public List<Doctor> searchDoctorInClinic(Long clinicId, String name) {
        List<Doctor> doctors = doctorRepository.findByClinicIdAndUserFirstNameContainingIgnoreCase(clinicId, name);
        return normalizeDoctors(doctors);
    }

    // ==================== DOKTOR OLUŞTURMA (Admin için) ====================

    @Transactional
    public Map<String, Object> createDoctorWithUser(
            String firstName,
            String lastName,
            String specialization,
            Long clinicId) {

        // Ünvan doğrulama
        String validatedSpecialization = null;
        if (specialization != null && !specialization.isEmpty()) {
            validatedSpecialization = validateAndFixTitle(specialization);
        }

        String username = generateUsername(firstName, lastName);
        String plainPassword = generateRandomPassword();
        String tempEmail = generateTempEmail(firstName, lastName);
        String tempTckn = generateRandomTckn();

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

        var clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new RuntimeException("Klinik bulunamadı: " + clinicId));

        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setSpecialization(validatedSpecialization != null ? validatedSpecialization : "Uzmanlık Belirtilmemiş");
        doctor.setClinic(clinic);

        Doctor savedDoctor = doctorRepository.save(doctor);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedDoctor.getId());
        response.put("firstName", savedDoctor.getUser().getFirstName());
        response.put("lastName", savedDoctor.getUser().getLastName());
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
        Doctor savedDoctor = doctorRepository.save(doctor);
        return normalizeDoctor(savedDoctor);
    }

    @Transactional
    public void updateDoctorOwnProfile(String currentTckn, DoctorProfileRequest request) {
        // 1. Doktoru ve bağlı User nesnesini bul
        Doctor doctor = doctorRepository.findByUserTckn(currentTckn)
                .orElseThrow(() -> new EntityNotFoundException("Doktor kaydı bulunamadı."));

        User user = doctor.getUser();

        // 2. TCKN Güncelleme Kontrolleri
        if (request.getTckn() != null && !request.getTckn().isBlank()) {
            String newTckn = request.getTckn().trim();
            if (newTckn.length() != 11 || !newTckn.matches("\\d+")) {
                throw new BadRequestException("TC Kimlik No 11 haneli ve rakamlardan oluşmalıdır.");
            }
            if (!user.getTckn().equals(newTckn) && userRepository.existsByTckn(newTckn)) {
                throw new BadRequestException("Bu TC Kimlik Numarası zaten kullanımda.");
            }
            user.setTckn(newTckn);
        }

        // 3. Email Güncelleme
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
                throw new BadRequestException("Bu e-posta adresi zaten kullanılıyor.");
            }
            user.setEmail(newEmail);
        }

        // 4. Username Güncelleme
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            if (!user.getUsername().equals(newUsername) && userRepository.existsByUsername(newUsername)) {
                throw new BadRequestException("Bu kullanıcı adı zaten alınmış.");
            }
            user.setUsername(newUsername);
        }

    

        // Kaydetme işlemleri (@Transactional sayesinde hata olursa hepsi geri alınır)
        userRepository.save(user);
        doctorRepository.save(doctor);
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
        doctorRepository.flush();

        // KULLANICIYI DA SİL
        userRepository.deleteById(userId);
        userRepository.flush();
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

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
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