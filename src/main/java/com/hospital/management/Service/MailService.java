package com.hospital.management.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetMail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        
        message.setFrom("akilli.kutuphane6@gmail.com"); 
        message.setTo(to);
        
       
        message.setSubject("Hastane Yönetim Sistemi | Şifre Sıfırlama Talebi");
        
        // 3. Mesaj içeriği ve Frontend linki
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        
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
}
