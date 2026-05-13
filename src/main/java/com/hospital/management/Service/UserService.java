package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.DTO.RegisterRequest;
import com.hospital.management.DTO.UserResponse;
import com.hospital.management.Entity.Appointment;
import com.hospital.management.Entity.Doctor;
import com.hospital.management.Entity.Patient;
import com.hospital.management.Entity.Penalty;
import com.hospital.management.Entity.Role;
import com.hospital.management.Entity.Slot;
import com.hospital.management.Entity.SlotStatus;
import com.hospital.management.Entity.User;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Exception.EntityNotFoundException;
import com.hospital.management.Repository.DoctorRepository;
import com.hospital.management.Repository.UserRepository;
import com.hospital.management.Repository.PatientRepository;
import com.hospital.management.Repository.SlotRepository;
import com.hospital.management.Repository.AppointmentRepository;
import com.hospital.management.Repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final PenaltyRepository penaltyRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public UserResponse getById(Long id) {
        if (!SecurityUtil.isOwner(id) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu kullanıcı bilgilerine erişim yetkiniz yok.");
        }
        return mapToResponse(getUserById(id));
    }

    @Transactional
    public ResponseEntity<?> updateProfileFields(String tckn, Map<String, String> updates) {
        try {
            User user = getUserByTckn(tckn);

            if (!SecurityUtil.isOwner(user.getId()) && !SecurityUtil.isAdmin()) {
                throw new AccessDeniedException("Yetkiniz yok.");
            }

            // SADECE GELEN ALANLARI GÜNCELLE
            if (updates.containsKey("username") && updates.get("username") != null) {
                String newUsername = updates.get("username").trim();
                if (!newUsername.isEmpty()) {
                    if (newUsername.length() < 3) {
                        throw new BadRequestException("Kullanıcı adı en az 3 karakter olmalıdır.");
                    }
                    if (userRepository.existsByUsername(newUsername) && !user.getUsername().equals(newUsername)) {
                        throw new BadRequestException("Bu kullanıcı adı zaten kullanılıyor");
                    }
                    user.setUsername(newUsername);
                }
            }

            if (updates.containsKey("email") && updates.get("email") != null) {
                String newEmail = updates.get("email").trim();
                if (!newEmail.isEmpty()) {
                    if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
                        throw new BadRequestException("Bu e-posta adresi zaten kullanılıyor");
                    }
                    user.setEmail(newEmail);
                }
            }

            if (updates.containsKey("firstName") && updates.get("firstName") != null) {
                String newFirstName = updates.get("firstName").trim();
                if (!newFirstName.isEmpty()) {
                    user.setFirstName(newFirstName);
                }
            }

            if (updates.containsKey("lastName") && updates.get("lastName") != null) {
                String newLastName = updates.get("lastName").trim();
                if (!newLastName.isEmpty()) {
                    user.setLastName(newLastName);
                }
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Profil başarıyla güncellendi",
                    "user", mapToResponse(user)));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional
    public void updatePassword(String tckn, String currentPassword, String newPassword) {

        User user = userRepository.findByTckn(tckn)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));

        // 2. Mevcut şifre doğruluğunu kontrol et
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mevcut şifreniz hatalı!");
        }

        // 3. Yeni şifre eski şifreyle aynı mı kontrolü
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("Yeni şifre eskisinden farklı olmalıdır!");
        }

        // 4. Yeni şifreyi hashleyerek kaydet
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateByTckn(String tckn, RegisterRequest request) {
        User user = getUserByTckn(tckn);

        if (!SecurityUtil.isOwner(user.getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Yetkiniz yok.");
        }

        validateUpdate(user, request);

        if (user.getRole() != Role.PATIENT && request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        user.setTckn(request.getTckn());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void updateDoctorProfile(String tckn, String email, String newTckn, String username) {
        User user = userRepository.findByTckn(tckn)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));

        if (!SecurityUtil.isOwner(user.getId()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Profilinizi güncelleme yetkiniz yok.");
        }

        // Username güncelleme
        if (username != null && !username.equals(user.getUsername())) {
            if (username.length() < 3) {
                throw new BadRequestException("Kullanıcı adı en az 3 karakter olmalıdır.");
            }
            if (userRepository.existsByUsername(username)) {
                throw new BadRequestException("Bu kullanıcı adı zaten kullanılıyor.");
            }
            user.setUsername(username);
        }

        // Email güncelleme
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new BadRequestException("Bu e-posta adresi zaten kullanılıyor.");
            }
            user.setEmail(email);
        }

        // TCKN güncelleme
        if (newTckn != null && !newTckn.equals(user.getTckn())) {
            if (newTckn.length() != 11 || !newTckn.matches("\\d+")) {
                throw new BadRequestException("TC Kimlik No 11 haneli ve sadece rakamlardan oluşmalıdır.");
            }
            if (userRepository.existsByTckn(newTckn)) {
                throw new BadRequestException("Bu TC Kimlik Numarası zaten kayıtlı.");
            }
            user.setTckn(newTckn);
        }

        userRepository.save(user);
    }

    @Transactional
    public void updateEmailByTckn(String tckn, String newEmail) {

        User user = getUserByTckn(tckn);

        // 2. Yeni email başka bir kullanıcıda var mı kontrol et
        if (userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("Bu e-posta adresi zaten başka bir kullanıcı tarafından kullanılıyor.");
        }
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    public UserResponse getByTckn(String tckn) {
        User user = getUserByTckn(tckn);
        return mapToResponse(user);
    }

    @Transactional
    public void deleteById(Long id) {
        // Yetki kontrolü
        if (!SecurityUtil.isOwner(id) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Bu hesabı silme yetkiniz yok.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı (ID: " + id + ")"));

        try {
            // ============ HASTA İSE ============
            if (user.getPatient() != null) {
                Patient patient = user.getPatient();

                // ... cezaları silme kısmı aynı kalabilir ...

                List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
                for (Appointment appointment : appointments) {
                    Slot slot = appointment.getSlot();
                    if (slot != null) {
                        // İLİŞKİYİ KOPAR: Randevunun slotla bağını kes
                        appointment.setSlot(null);
                        slot.setAppointment(null); // Çift taraflı koparma

                        slot.setStatus(SlotStatus.AVAILABLE);
                        slotRepository.save(slot);
                    }
                    appointmentRepository.delete(appointment);
                }
                appointmentRepository.flush(); // Randevuların silindiğinden emin ol
                patientRepository.delete(patient);
            }

            // ============ DOKTOR İSE ============
            if (user.getDoctor() != null) {
                Doctor doctor = user.getDoctor();

                List<Slot> slots = slotRepository.findByDoctorId(doctor.getId());
                for (Slot slot : slots) {
                    if (slot.getAppointment() != null) {
                        Appointment app = slot.getAppointment();

                        // İLİŞKİYİ KOPAR: Önce slotun randevu bağını null yap
                        slot.setAppointment(null);
                        slotRepository.saveAndFlush(slot); // Veritabanına yansıt

                        appointmentRepository.delete(app);
                        appointmentRepository.flush(); // Silme işlemini onayla
                    }
                    slotRepository.delete(slot);
                }
                doctorRepository.delete(doctor);
            }
            // ============ KULLANICIYI SİL ============
            userRepository.delete(user);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Kullanıcı silinirken hata oluştu: " + e.getMessage());
        }
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı (ID: " + id + ")"));
    }

    private User getUserByTckn(String tckn) {
        return userRepository.findByTckn(tckn)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı (TCKN: " + tckn + ")"));
    }

    private void validateUpdate(User user, RegisterRequest request) {
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email kullanımda");
        if (!user.getTckn().equals(request.getTckn()) && userRepository.existsByTckn(request.getTckn()))
            throw new BadRequestException("Bu TC Kimlik Numarası zaten kayıtlı.");
    }

    private UserResponse mapToResponse(User user) {
        String bloodGroup = "Belirtilmedi";
        Integer age = null;

        if (user.getPatient() != null) {
            if (user.getPatient().getBloodType() != null) {
                bloodGroup = user.getPatient().getBloodType().name();
            }
            age = user.getPatient().getAge();
        }

        return new UserResponse(
                user.getId(),
                user.getTckn(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                bloodGroup,
                age);
    }
}