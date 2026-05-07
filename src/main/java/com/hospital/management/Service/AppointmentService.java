package com.hospital.management.Service;

import com.hospital.management.Entity.*;
import com.hospital.management.Repository.*;
import com.hospital.management.Config.SecurityUtil;
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

    // --------------------------
    // CREATE APPOINTMENT
    // --------------------------
    @Transactional
    public Appointment createAppointment(Long patientId, Long slotId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı"));

        assertOwnerOrAdmin(patient.getUser().getUsername());

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot bulunamadı"));

        if (slot.getStatus() != SlotStatus.AVAILABLE)
            throw new RuntimeException("Slot dolu");

        if (penaltyService.hasActivePenalty(patientId))
            throw new RuntimeException("Ceza var");

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.APPROVED);

        slot.setStatus(SlotStatus.BOOKED);

        slotRepository.save(slot);
        return appointmentRepository.save(appointment);
    }

    // --------------------------
    // PATIENT (SELF OR ADMIN)
    // --------------------------
    public List<Appointment> getPatientAppointments(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta yok"));

        assertOwnerOrAdmin(patient.getUser().getUsername());

        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getPatientPastAppointments(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta yok"));

        assertOwnerOrAdmin(patient.getUser().getUsername());

        return appointmentRepository.findByPatientIdAndSlotStartTimeBefore(
                patientId, LocalDateTime.now());
    }

    // --------------------------
    // DOCTOR (SELF OR ADMIN)
    // --------------------------
    public List<Appointment> getDoctorAppointments(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor yok"));

        assertOwnerOrAdmin(doctor.getUser().getUsername());

        return appointmentRepository.findBySlotDoctorId(doctorId);
    }

    public List<Appointment> getDoctorActiveAppointments(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor yok"));

        assertOwnerOrAdmin(doctor.getUser().getUsername());

        return appointmentRepository.findBySlotDoctorIdAndStatus(
                doctorId, AppointmentStatus.APPROVED);
    }

    // --------------------------
    // CANCEL
    // --------------------------
    @Transactional
    public void cancelAppointment(Long appointmentId) {

        Appointment appointment = getAppointmentOrThrow(appointmentId);

        assertOwnerOrAdmin(
                appointment.getPatient().getUser().getUsername());

        LocalDateTime start = appointment.getSlot().getStartTime();

        if (start.isBefore(LocalDateTime.now()))
            throw new RuntimeException("Geçmiş iptal edilemez");

        if (start.isBefore(LocalDateTime.now().plusHours(24)))
            throw new RuntimeException("24 saat kuralı");

        appointment.setStatus(AppointmentStatus.CANCELED);

        Slot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);

        slotRepository.save(slot);
        appointmentRepository.save(appointment);
    }

    // --------------------------
    // NOT ATTENDED
    // --------------------------
    @Transactional
    public void markAsNotAttended(Long appointmentId) {

        Appointment appointment = getAppointmentOrThrow(appointmentId);

        boolean isDoctor = SecurityUtil.isOwner(
                appointment.getSlot().getDoctor().getUser().getUsername());

        if (!SecurityUtil.isAdmin() && !isDoctor)
            throw new RuntimeException("Yetkisiz");

        if (appointment.getSlot().getStartTime().isAfter(LocalDateTime.now()))
            throw new RuntimeException("Gelecek işaretlenemez");

        appointment.setStatus(AppointmentStatus.NOT_ATTENDED);

        penaltyService.createPenaltyFromAppointment(appointment);

        appointmentRepository.save(appointment);
    }

    // --------------------------
    // ADMIN ONLY
    // --------------------------
    public List<Appointment> getClinicAppointments(Long clinicId) {

        if (!SecurityUtil.isAdmin())
            throw new RuntimeException("Sadece admin");

        return appointmentRepository.findBySlotDoctorClinicId(clinicId);
    }

    // --------------------------
    // CURRENT USER - PATIENT
    // --------------------------
    public List<Appointment> getMyAppointments() {

        String username = SecurityUtil.getCurrentUsername();

        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Hasta yok"));

        return appointmentRepository.findByPatientId(patient.getId());
    }

    // --------------------------
    // CURRENT USER - DOCTOR
    // --------------------------
    public List<Appointment> getMyDoctorAppointments() {

        String username = SecurityUtil.getCurrentUsername();

        Doctor doctor = doctorRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Doktor yok"));

        return appointmentRepository.findBySlotDoctorId(doctor.getId());
    }

    // --------------------------
    // HELPERS
    // --------------------------
    private void assertOwnerOrAdmin(String entityUsername) {
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isOwner(entityUsername)) {
            throw new RuntimeException("Yetkisiz işlem");
        }
    }

    private Appointment getAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu yok"));
    }
}