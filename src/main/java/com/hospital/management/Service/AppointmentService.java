package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.DTO.AppointmentResponseDTO;
import com.hospital.management.Entity.*;
import com.hospital.management.Repository.*;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PenaltyService penaltyService;
    private final MailService mailService;
    private final UserRepository userRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // CREATE
  @Transactional
    public Appointment createAppointment(Long patientId, Long slotId) {

        Patient patient = patientRepository.findByUserId(patientId) 
                .orElseThrow(() -> new EntityNotFoundException("Bu kullanıcıya ait hasta kaydı bulunamadı"));

        assertOwnerOrAdmin(patient.getUser().getId());

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Seçilen randevu saati bulunamadı."));

        // --- KONTROL 1: AYNI SAATTE ÇAKIŞMA (DETAYLI MESAJ) ---
        appointmentRepository.findConflictDetail(patient.getId(), slot.getStartTime()).ifPresent(conflict -> {
            String hName = conflict.getSlot().getDoctor().getClinic().getHospital().getName();
            String cName = conflict.getSlot().getDoctor().getClinic().getName();
            throw new BadRequestException("Bu saatte zaten " + hName + " (" + cName + ") randevunuz bulunuyor.");
        });

        // --- KONTROL 2: AYNI GÜN İÇİNDE BAŞKA RANDEVU (GÜNLÜK ENGEL) ---
        if (appointmentRepository.hasAnyAppointmentOnDate(patient.getId(), slot.getStartTime())) {
            throw new BadRequestException("Aynı gün içerisinde sadece bir randevu alabilirsiniz. Lütfen mevcut randevunuzu iptal ediniz.");
        }

        // --- KONTROL 3: SLOT DURUMU ---
        if (slot.getStatus() != SlotStatus.AVAILABLE)
            throw new BadRequestException("Bu randevu saati az önce başkası tarafından alındı.");

        // --- KONTROL 4: CEZA DURUMU ---
        if (penaltyService.hasActivePenalty(patient.getId())) // patientId yerine patient.getId() kullandık (ID eşleme için)
            throw new BadRequestException("Sisteme gelmediğiniz randevular nedeniyle geçici süreyle randevu alamazsınız.");

        // --- RANDEVU OLUŞTURMA ---
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.APPROVED);

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        // Mail gönderimi (Senin mevcut kodun)
        try {
            mailService.sendAppointmentConfirmationMail(
                    patient.getUser().getEmail(),
                    patient.getUser().getFirstName() + " " + patient.getUser().getLastName(),
                    slot.getDoctor().getUser().getFirstName() + " " + slot.getDoctor().getUser().getLastName(),
                    slot.getDoctor().getClinic().getName(),
                    slot.getStartTime().toString());
        } catch (Exception e) {
            System.err.println("Randevu onay maili gönderilirken hata oluştu: " + e.getMessage());
        }

        return appointmentRepository.save(appointment);
    }
    // HASTA RANDEVULARI
// 1. TÜM RANDEVULAR
public List<AppointmentResponseDTO> getPatientAppointments(Long userId) {
    assertOwnerOrAdmin(userId);
    List<Appointment> appointments = appointmentRepository.findByPatient_User_Id(userId);
    return convertToDtoList(appointments);
}

// 2. GEÇMİŞ RANDEVULAR
public List<AppointmentResponseDTO> getPatientPastAppointments(Long userId) {
    assertOwnerOrAdmin(userId);
    List<Appointment> appointments = appointmentRepository.findByPatient_User_IdAndSlot_StartTimeBefore(
            userId, LocalDateTime.now());
    return convertToDtoList(appointments);
}

// --- YARDIMCI DÖNÜŞTÜRÜCÜ (Private Mapper) ---
private List<AppointmentResponseDTO> convertToDtoList(List<Appointment> appointments) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    
    return appointments.stream().map(app -> {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(app.getId());
        dto.setStatus(app.getStatus().toString());

        // 1. ZİNCİRİ BURADA KURUYORUZ (Hatanın çözümü burası):
        if (app.getSlot() != null && app.getSlot().getDoctor() != null) {
            
            // İşte burada 'user' değişkenini tanımlıyoruz:
            var doctor = app.getSlot().getDoctor();
            var user = doctor.getUser(); 

            // Şimdi System.out.println hata vermeyecek!
            System.out.println("DTO Hazırlanıyor - Doktor: " + user.getFirstName());
            
            dto.setDoctorName("Dr. " + user.getFirstName() + " " + user.getLastName());
            dto.setClinicName(doctor.getClinic() != null ? doctor.getClinic().getName() : "Klinik Yok");
            
            // SAAT SORUNUNUN ÇÖZÜMÜ: LocalDateTime.now() DEĞİL, app içindeki saati alıyoruz
            dto.setAppointmentDate(app.getSlot().getStartTime().format(formatter));
            
            dto.setCanCancel(app.getSlot().getStartTime().isAfter(LocalDateTime.now()));
        } else {
            dto.setDoctorName("Bilinmeyen Doktor");
            dto.setClinicName("Bilinmeyen Klinik");
            dto.setAppointmentDate("Tarih Yok");
        }
        
        return dto;
    }).collect(Collectors.toList());
}




    // DOCTOR
    public List<Appointment> getDoctorAppointments(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doktor yok"));

        assertOwnerOrAdmin(doctor.getUser().getId());

        return appointmentRepository.findByDoctorIdWithDetails(doctorId);
    }

    public List<Appointment> getDoctorActiveAppointments(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doktor yok"));

        assertOwnerOrAdmin(doctor.getUser().getId());

        return appointmentRepository.findBySlotDoctorIdAndStatus(
                doctorId, AppointmentStatus.APPROVED);
    }

    // CANCEL
    @Transactional
    public void cancelAppointment(Long appointmentId) {

        Appointment appointment = getAppointmentOrThrow(appointmentId);

        assertOwnerOrAdmin(appointment.getPatient().getUser().getId());

        LocalDateTime start = appointment.getSlot().getStartTime();

        if (start.isBefore(LocalDateTime.now()))
            throw new BadRequestException("Geçmiş iptal edilemez");

        if (start.isBefore(LocalDateTime.now().plusHours(24)))
            throw new BadRequestException("24 saat kuralı");

        appointment.setStatus(AppointmentStatus.CANCELED);

        Slot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);

        slotRepository.save(slot);
        appointmentRepository.save(appointment);
    }

    // NOT ATTENDED
    @Transactional
    public void markAsNotAttended(Long appointmentId) {

        Appointment appointment = getAppointmentOrThrow(appointmentId);

        boolean isDoctor = SecurityUtil.isOwner(
                appointment.getSlot().getDoctor().getUser().getId());

        if (!SecurityUtil.isAdmin() && !isDoctor)
            throw new AccessDeniedException("Yetkisiz işlem");

        if (appointment.getSlot().getStartTime().isAfter(LocalDateTime.now()))
            throw new BadRequestException("Gelecek randevu işaretlenemez");

        appointment.setStatus(AppointmentStatus.NOT_ATTENDED);

        penaltyService.createPenaltyFromAppointment(appointment);

        // Hastaya ceza maili gönder
        mailService.sendPenaltyMail(
                appointment.getPatient().getUser().getEmail(),
                appointment.getPatient().getUser().getFirstName() + " "
                        + appointment.getPatient().getUser().getLastName(),
                appointment.getSlot().getStartTime().toString());

        appointmentRepository.save(appointment);
    }

    // ADMIN ONLY
    public List<Appointment> getClinicAppointments(Long clinicId) {

        if (!SecurityUtil.isAdmin())
            throw new AccessDeniedException("Sadece admin erişebilir");

        return appointmentRepository.findBySlotDoctorClinicId(clinicId);
    }

 public List<AppointmentResponseDTO> getMyAppointments(Long userId) {
    List<Appointment> appointments = appointmentRepository.findByPatient_User_Id(userId);
    
    if (appointments == null || appointments.isEmpty()) {
        throw new EntityNotFoundException("Bu kullanıcıya ait randevu kaydı bulunamadı.");
    }
    
    return convertToDtoList(appointments);
}


    public List<Appointment> getMyDoctorAppointments() {
        String username = SecurityUtil.getCurrentUsername();
       
        // Önce user'ı username ile bul (TCKN veya username)
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByTckn(username).orElse(null));

        if (user == null) {
            System.err.println("❌ Kullanıcı bulunamadı: " + username);
            throw new EntityNotFoundException("Kullanıcı bulunamadı: " + username);
        }

       
        // Doctor'u user ID ile bul
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    return new EntityNotFoundException("Doktor bulunamadı. User ID: " + user.getId());
                });

       
        // Randevuları getir
        List<Appointment> appointments = appointmentRepository.findBySlotDoctorId(doctor.getId());
      
        return appointments;
    }

    // HELPERS
    private void assertOwnerOrAdmin(Long userId) {

        if (SecurityUtil.isAdmin())
            return;

        if (!SecurityUtil.isOwner(userId)) {
            throw new AccessDeniedException("Yetkisiz işlem");
        }
    }

    private Appointment getAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Randevu yok"));
    }
}