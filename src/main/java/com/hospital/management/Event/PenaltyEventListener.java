package com.hospital.management.Event;

import com.hospital.management.Entity.Penalty;
import com.hospital.management.Entity.User;
import com.hospital.management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PenaltyEventListener {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void handlePenaltyCreated(PenaltyEvent event) {
        Penalty penalty = event.getPenalty();

        try {
            User patient = penalty.getPatient().getUser();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(patient.getEmail());
            message.setSubject("⚠️ Hastane Randevu Sistemi - Ceza Bilgilendirmesi");
            message.setText(
                    "Sayın " + patient.getFirstName() + " " + patient.getLastName() + ",\n\n" +
                            "Randevunuza katılmadığınız için 7 günlük ceza almış bulunmaktasınız.\n" +
                            "Ceza başlangıç tarihi: " + penalty.getPenaltyStartDate() + "\n" +
                            "Ceza bitiş tarihi: " + penalty.getPenaltyEndDate() + "\n\n" +
                            "Bu süre içerisinde yeni randevu alamazsınız.\n\n" +
                            "Sağlıklı günler dileriz.\n" +
                            "MedSoft Hastane Yönetim Sistemi");

            mailSender.send(message);
            System.out.println("✅ Ceza maili gönderildi: " + patient.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Ceza maili gönderilemedi: " + e.getMessage());
        }
    }
}