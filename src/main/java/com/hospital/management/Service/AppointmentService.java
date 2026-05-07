package com.hospital.management.Service;

import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.Entity.*;
import com.hospital.management.Repository.*;
import com.hospital.management.Exception.AccessDeniedException;
import com.hospital.management.Exception.BadRequestException;
import com.hospital.management.Exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PenaltyService penaltyService;

    // CREATE
    @Transactional
    public Appointment createAppointment(Long patientId, Long slotId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Hasta bulunamadı"));

        assertOwnerOrAdmin(patient.getUser().getId());

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot bulunamadı"));

        if (slot.getStatus() != SlotStatus.AVAILABLE)
            throw new BadRequestException("Slot dolu");

        if (penaltyService.hasActivePenalty(patientId))
            throw new BadRequestException("Aktif ceza var");

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.APPROVED);

        slot.setStatus(SlotStatus.BOOKED);

        slotRepository.save(slot);
        return appointmentRepository.save(appointment);
    }

    // PATIENT
    public List<Appointment> getPatientAppointments(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Hasta yok"));

        assertOwnerOrAdmin(patient.getUser().getId());

        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getPatientPastAppointments(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Hasta yok"));

        assertOwnerOrAdmin(patient.getUser().getId());

        return appointmentRepository.findByPatientIdAndSlotStartTimeBefore(
                patientId, LocalDateTime.now());
    }

    // DOCTOR
    public List<Appointment> getDoctorAppointments(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doktor yok"));

        assertOwnerOrAdmin(doctor.getUser().getId());

        return appointmentRepository.findBySlotDoctorId(doctorId);
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

        appointmentRepository.save(appointment);
    }

    // ADMIN ONLY
    public List<Appointment> getClinicAppointments(Long clinicId) {

        if (!SecurityUtil.isAdmin())
            throw new AccessDeniedException("Sadece admin erişebilir");

        return appointmentRepository.findBySlotDoctorClinicId(clinicId);
    }

    // MY APPOINTMENTS
    public List<Appointment> getMyAppointments() {

        String username = SecurityUtil.getCurrentUsername();

        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Hasta yok"));

        return appointmentRepository.findByPatientId(patient.getId());
    }

    public List<Appointment> getMyDoctorAppointments() {

        String username = SecurityUtil.getCurrentUsername();

        Doctor doctor = doctorRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Doktor yok"));

        return appointmentRepository.findBySlotDoctorId(doctor.getId());
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