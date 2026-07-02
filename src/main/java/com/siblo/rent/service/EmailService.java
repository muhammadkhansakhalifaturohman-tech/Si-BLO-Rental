package com.siblo.rent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Kirim email OTP pakai Brevo REST API (bukan SMTP).
 * Tidak butuh spring-boot-starter-mail sama sekali.
 */
@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.mail.from}")
    private String fromEmail;

    @Value("${brevo.mail.from-name}")
    private String fromName;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            String body = """
                {
                    "sender": {
                        "email": "%s",
                        "name": "%s"
                    },
                    "to": [{ "email": "%s" }],
                    "subject": "Kode OTP Registrasi SI-BLO",
                    "textContent": "Halo!\\n\\nKode OTP kamu untuk registrasi SI-BLO adalah:\\n\\n[ %s ]\\n\\nKode ini valid selama 5 menit.\\nJangan bagikan kode ini ke siapapun.\\n\\nSalam,\\nTim SI-BLO"
                }
                """.formatted(fromEmail, fromName, toEmail, otpCode);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new RuntimeException("Brevo API error: " + response.statusCode() + " " + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("Gagal kirim OTP email: " + e.getMessage(), e);
        }
    }
}
