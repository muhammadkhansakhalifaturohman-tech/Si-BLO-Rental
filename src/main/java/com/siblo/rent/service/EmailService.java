package com.siblo.rent.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private final String sendGridApiKey;
    private final String mailFrom;

    public EmailService(@Value("${SENDGRID_API_KEY:}") String sendGridApiKey,
                        @Value("${MAIL_FROM:muhammadkhansakhalifaturohman@gmail.com}") String mailFrom) {
        this.sendGridApiKey = sendGridApiKey;
        this.mailFrom = mailFrom;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        Email from = new Email(mailFrom);
        String subject = "Kode OTP Registrasi SI-BLO";
        Email to = new Email(toEmail);
        Content content = new Content("text/plain",
            "Halo!\n\n" +
            "Kode OTP kamu untuk registrasi SI-BLO adalah:\n\n" +
            "[ " + otpCode + " ]\n\n" +
            "Kode ini valid selama 5 menit.\n" +
            "Jangan bagikan kode ini ke siapapun.\n\n" +
            "Salam,\nTim SI-BLO"
        );
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid API error: " + response.getStatusCode() +
                    " " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to send email via SendGrid: " + e.getMessage(), e);
        }
    }
}