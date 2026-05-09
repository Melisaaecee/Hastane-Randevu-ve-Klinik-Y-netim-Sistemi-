package com.hospital.management.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.management.Entity.User;
import com.hospital.management.Repository.UserRepository;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Transactional
    public void sendPasswordResetMail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("akilli.kutuphane6@gmail.com");
        message.setTo(to);

        message.setSubject("Hastane Yönetim Sistemi | Şifre Sıfırlama Talebi");

        // MailService.java
        String resetUrl = "http://127.0.0.1:5500/src/main/resources/static/resetPassword.html?token=" + token;
        String emailContent = "Sayın Kullanıcımız,\n\n" +
                "Hesabınız için şifre sıfırlama talebinde bulundunuz. " +
                "Aşağıdaki bağlantıya tıklayarak yeni şifrenizi belirleyebilirsiniz:\n\n" +
                resetUrl + "\n\n" +
                "Bu bağlantı 15 dakika süreyle geçerlidir.\n" +
                "Eğer bu talebi siz yapmadıysanız, lütfen bu e-postayı dikkate almayınız.\n\n" +
                "Sağlıklı günler dileriz,\nHastane Yönetim Sistemi Ekibi";

        message.setText(emailContent);

        mailSender.send(message);
    }

    @Transactional
    public void sendVerificationCode(String to, String tckn) {
        // 1. Kodu üret
        Random random = new Random();
        String code = String.valueOf(100000 + random.nextInt(900000));

        // 2. Kodu Veritabanına Kaydet (Doğrulama için)
        User user = userRepository.findByTckn(tckn)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setVerificationCode(code);
        userRepository.save(user);

        // 3. Maili Gönder
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("akilli.kutuphane6@gmail.com");
        message.setTo(to);
        message.setSubject("E-posta Güncelleme Doğrulama Kodu");
        message.setText("Doğrulama kodunuz: " + code);

        mailSender.send(message);
    }
}
