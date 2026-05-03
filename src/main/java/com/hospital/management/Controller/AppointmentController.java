package com.hospital.management.Controller;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // RANDEVU OLUŞTUR
    @PostMapping
    public Appointment create(@RequestParam Long patientId,
                              @RequestParam Long slotId) {
        return appointmentService.createAppointment(patientId, slotId);
    }

    //HASTA
    @GetMapping("/patient/{patientId}")
    public List<Appointment> getPatientAppointments(@PathVariable Long patientId) {
        return appointmentService.getPatientAppointments(patientId);
    }

    @GetMapping("/patient/{patientId}/past")
    public List<Appointment> getPast(@PathVariable Long patientId) {
        return appointmentService.getPatientPastAppointments(patientId);
    }

    // DOKTOR
    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getDoctorAppointments(@PathVariable Long doctorId) {
        return appointmentService.getDoctorAppointments(doctorId);
    }

    //İPTAL
    @PutMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
    }

    //GELMEDİ (CEZA TETİKLER)
    @PutMapping("/{id}/not-attended")
    public void markNotAttended(@PathVariable Long id) {
        appointmentService.markAsNotAttended(id);
    }
}