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

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional
    public Appointment createAppointment(Long patientId, Long slotId) {
        Patient patient = patientRepository.findByUserId(patientId) 
                .orElseThrow(() -> new EntityNotFoundException("Bu kullanıcıya ait hasta kaydı bulunamadı"));

        assertOwnerOrAdmin(patient.getUser().getId());
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Seçilen randevu saati bulunamadı."));

        appointmentRepository.findConflictDetail(patient.getId(), slot.getStartTime()).ifPresent(conflict -> {
            String hName = conflict.getSlot().getDoctor().getClinic().getHospital().getName();
            String cName = conflict.getSlot().getDoctor().getClinic().getName();
            throw new BadRequestException("Bu saatte zaten " + hName + " (" + cName + ") randevunuz bulunuyor.");
        });

        if (appointmentRepository.hasAnyAppointmentOnDate(patient.getId(), slot.getStartTime())) {
            throw new BadRequestException("Aynı gün içerisinde sadece bir randevu alabilirsiniz.");
        }

        if (slot.getStatus() != SlotStatus.AVAILABLE)
            throw new BadRequestException("Bu randevu saati az önce başkası tarafından alındı.");

        if (penaltyService.hasActivePenalty(patient.getId()))
            throw new BadRequestException("Sisteme gelmediğiniz randevular nedeniyle randevu alamazsınız.");

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.APPROVED);

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        try {
            mailService.sendAppointmentConfirmationMail(
                    patient.getUser().getEmail(),
                    patient.getUser().getFirstName() + " " + patient.getUser().getLastName(),
                    slot.getDoctor().getUser().getFirstName() + " " + slot.getDoctor().getUser().getLastName(),
                    slot.getDoctor().getClinic().getName(),
                    slot.getStartTime().toString());
        } catch (Exception e) {
            System.err.println("Mail hatası: " + e.getMessage());
        }

        return appointmentRepository.save(appointment);
    }

    // BURASI DÜZELTİLDİ: Artık dolu veriyi çeken Query'i kullanıyor
    public List<AppointmentResponseDTO> getMyAppointments(Long userId) {
        List<Appointment> appointments = appointmentRepository.findAllByUserIdWithDetails(userId);
        if (appointments == null || appointments.isEmpty()) {
            throw new EntityNotFoundException("Bu kullanıcıya ait randevu kaydı bulunamadı.");
        }
        return convertToDtoList(appointments);
    }

    public List<AppointmentResponseDTO> getPatientAppointments(Long patientId) {
        // PatientId aslında userId olarak geliyor, o yüzden findAllByUserIdWithDetails güvenli
        List<Appointment> appointments = appointmentRepository.findAllByUserIdWithDetails(patientId);
        return convertToDtoList(appointments);
    }

    public List<AppointmentResponseDTO> getPatientPastAppointments(Long userId) {
        List<Appointment> appointments = appointmentRepository.findPastByUserIdWithDetails(userId, LocalDateTime.now());
        return convertToDtoList(appointments);
    }

    private List<AppointmentResponseDTO> convertToDtoList(List<Appointment> appointments) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        
        return appointments.stream().map(app -> {
            AppointmentResponseDTO dto = new AppointmentResponseDTO();
            dto.setId(app.getId());
            dto.setStatus(app.getStatus().toString());

            if (app.getSlot() != null && app.getSlot().getDoctor() != null) {
                var doctor = app.getSlot().getDoctor();
                var user = doctor.getUser(); 
                
                dto.setDoctorName("Dr. " + user.getFirstName() + " " + user.getLastName());
                dto.setClinicName(doctor.getClinic() != null ? doctor.getClinic().getName() : "Klinik Yok");
                dto.setAppointmentDate(app.getSlot().getStartTime().format(dtf));
                dto.setCanCancel(app.getSlot().getStartTime().isAfter(LocalDateTime.now()) && 
                                app.getStatus() == AppointmentStatus.APPROVED);
            } else {
                dto.setDoctorName("Bilinmeyen Doktor");
                dto.setClinicName("Bilinmeyen Klinik");
                dto.setAppointmentDate("Tarih Yok");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // İptal ve diğer metodlar aynen kalıyor...
    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Randevu yok"));
        assertOwnerOrAdmin(appointment.getPatient().getUser().getId());
        if (appointment.getSlot().getStartTime().isBefore(LocalDateTime.now().plusHours(24)))
            throw new BadRequestException("24 saat kuralı");
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.getSlot().setStatus(SlotStatus.AVAILABLE);
        appointmentRepository.save(appointment);
    }

    private void assertOwnerOrAdmin(Long userId) {
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isOwner(userId)) 
            throw new AccessDeniedException("Yetkisiz işlem");
    }

    public List<Appointment> getMyDoctorAppointments() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username).orElseThrow();
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElseThrow();
        return appointmentRepository.findBySlotDoctorId(doctor.getId());
    }

    public List<Appointment> getDoctorActiveAppointments(Long doctorId) {
        return appointmentRepository.findBySlotDoctorIdAndStatus(doctorId, AppointmentStatus.APPROVED);
    }

    public List<Appointment> getClinicAppointments(Long clinicId) {
        return appointmentRepository.findBySlotDoctorClinicId(clinicId);
    }
}