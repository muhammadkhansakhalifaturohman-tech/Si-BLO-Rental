package com.siblo.rent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String mailFrom;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(mailFrom);
        message.setSubject("Kode OTP Registrasi SI-BLO");
        message.setText(
            "Halo!\n\n" +
            "Kode OTP kamu untuk registrasi SI-BLO adalah:\n\n" +
            "[ " + otpCode + " ]\n\n" +
            "Kode ini valid selama 5 menit.\n" +
            "Jangan bagikan kode ini ke siapapun.\n\n" +
            "Salam,\nTim SI-BLO"
        );
        mailSender.send(message);
    }
}