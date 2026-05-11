package com.hospital.management.Controller;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Service.AppointmentService;
import com.hospital.management.Config.SecurityUtil;
import com.hospital.management.DTO.AppointmentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<?> create(@RequestParam Long userId, @RequestParam Long slotId) {
        try {
            Appointment appointment = appointmentService.createAppointment(userId, slotId);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponseDTO>> myAppointments() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(appointmentService.getMyAppointments(currentUserId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getPatientAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(patientId));
    }

    @GetMapping("/patient/{patientId}/past")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getPatientPastAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getPatientPastAppointments(patientId));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok("Randevu başarıyla iptal edildi.");
    }

    // Doktor ve Admin metodları aynen kalıyor...
    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> myDoctorAppointments() {
        return ResponseEntity.ok(appointmentService.getMyDoctorAppointments());
    }
}