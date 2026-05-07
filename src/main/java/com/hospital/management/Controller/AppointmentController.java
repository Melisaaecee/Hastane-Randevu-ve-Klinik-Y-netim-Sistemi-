package com.hospital.management.Controller;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // PATIENT
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public Appointment create(@RequestParam Long patientId,
            @RequestParam Long slotId) {
        return appointmentService.createAppointment(patientId, slotId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public List<Appointment> myAppointments() {
        return appointmentService.getMyAppointments();
    }

    // DOCTOR
    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<Appointment> myDoctorAppointments() {
        return appointmentService.getMyDoctorAppointments();
    }

    // ADMIN VIEWS
    @GetMapping("/admin/patient/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Appointment> patientAdmin(@PathVariable Long id) {
        return appointmentService.getPatientAppointments(id);
    }

    @GetMapping("/admin/doctor/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Appointment> doctorAdmin(@PathVariable Long id) {
        return appointmentService.getDoctorAppointments(id);
    }

    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Appointment> clinic(@PathVariable Long clinicId) {
        return appointmentService.getClinicAppointments(clinicId);
    }

    // ACTIONS
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public void cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
    }

    @PutMapping("/{id}/not-attended")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public void notAttended(@PathVariable Long id) {
        appointmentService.markAsNotAttended(id);
    }
}