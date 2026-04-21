package com.hospital.management.Service;

import com.hospital.management.Entity.*;
import com.hospital.management.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final PenaltyService penaltyService; // ✅ sadece service

    // 🔥 RANDEVU OLUŞTUR
    public Appointment createAppointment(Long patientId, Long slotId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı"));

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot bulunamadı"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new RuntimeException("Slot dolu");
        }

        // ❌ repository değil → service
        if (penaltyService.hasActivePenalty(patientId)) {
            throw new RuntimeException("Aktif cezanız var, randevu alamazsınız");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.APPROVED);

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getPatientPastAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdAndSlotStartTimeBefore(
                patientId, LocalDateTime.now());
    }

    public List<Appointment> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findBySlotDoctorId(doctorId);
    }

    public List<Appointment> getDoctorActiveAppointments(Long doctorId) {
        return appointmentRepository.findBySlotDoctorIdAndStatus(
                doctorId, AppointmentStatus.APPROVED);
    }

    public List<Appointment> getClinicAppointments(Long clinicId) {
        return appointmentRepository.findBySlotDoctorClinicId(clinicId);
    }

    public void cancelAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

        appointment.setStatus(AppointmentStatus.CANCELED);

        Slot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);

        slotRepository.save(slot);
        appointmentRepository.save(appointment);
    }

    // 🔥 GELMEDİ → CEZA ÜRET (DOĞRU MİMARİ)
    public void markAsNotAttended(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

        appointment.setStatus(AppointmentStatus.NOT_ATTENDED);

        // 🔥 CEZA TAMAMEN SERVICE İÇİNDE
        penaltyService.createPenaltyFromAppointment(appointment);

        appointmentRepository.save(appointment);
    }
}