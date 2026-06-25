package com.siblo.rent.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("si.bookinglapangonline@gmail.com");
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