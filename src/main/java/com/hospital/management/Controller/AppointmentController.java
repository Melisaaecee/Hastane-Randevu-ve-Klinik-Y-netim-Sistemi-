package com.hospital.management.Controller;

import com.hospital.management.Entity.Appointment;
import com.hospital.management.Service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // --- RANDEVU OLUŞTURMA VE KİŞİSEL GÖRÜNÜM (ME) ---

    // Randevu oluşturur. Hasta ID ve Slot ID parametreleri ile çalışır. 24 saat
    // kuralı ve uygunluk kontrolü serviste yapılır.
    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<Appointment> create(@RequestParam Long patientId, @RequestParam Long slotId) {
        return ResponseEntity.ok(appointmentService.createAppointment(patientId, slotId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    // Login olan hastanın kendi randevularını listeler.
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Appointment>> myAppointments() {
        return ResponseEntity.ok(appointmentService.getMyAppointments());
    }

    // Login olan doktorun kendi randevularını listeler.
    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> myDoctorAppointments() {
        System.out.println("=== /api/appointments/doctor/my çağrıldı ===");
        List<Appointment> appointments = appointmentService.getMyDoctorAppointments();
        System.out.println("Gönderilen randevu sayısı: " + appointments.size());
        return ResponseEntity.ok(appointments);
    }
    // --- HASTA VE DOKTOR ÖZEL GÖRÜNÜMLERİ (ID BAZLI) ---

    // Belirli bir hastanın tüm randevularını getirir (Admin ve Hasta görebilir).
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<Appointment>> getPatientAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(patientId));
    }

    // Belirli bir hastanın geçmiş randevularını getirir.
    @GetMapping("/patient/{patientId}/past")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<Appointment>> getPatientPastAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getPatientPastAppointments(patientId));
    }

    // Belirli bir hastanın aktif (gelecek) randevularını getirir.
    @GetMapping("/doctor/{doctorId}/active")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<Appointment>> getDoctorActiveAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorActiveAppointments(doctorId));
    }

    // --- AKSİYONLAR (İPTAL / GELMEDİ) ---

    // Randevuyu iptal eder. Sadece randevu sahibi hasta veya admin iptal edebilir.
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok("Randevu başarıyla iptal edildi.");
    }

    // Randevuyu "Gelmedi" olarak işaretler. Sadece doktor veya admin bu işlemi
    // yapabilir. Gelmedi olarak işaretlenen randevular için ceza işlemi uygulanır.
    @PutMapping("/{id}/not-attended")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<String> markAsNotAttended(@PathVariable Long id) {
        appointmentService.markAsNotAttended(id);
        return ResponseEntity.ok("Randevu 'Gelmedi' olarak işaretlendi ve ceza işlemi uygulandı.");
    }

    // --- ADMİN PANELİ GÖRÜNÜMLERİ ---

    // Belirli bir kliniğe ait tüm randevuları listeler (Admin görebilir).
    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getByClinic(@PathVariable Long clinicId) {
        return ResponseEntity.ok(appointmentService.getClinicAppointments(clinicId));
    }
}