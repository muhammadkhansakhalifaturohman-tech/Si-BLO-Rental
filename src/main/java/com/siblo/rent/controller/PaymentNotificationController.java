package com.siblo.rent.controller;

import com.siblo.rent.config.MidtransConfig;
import com.siblo.rent.service.PaymentService;
import com.siblo.rent.util.SignatureUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint ini dipanggil oleh SERVER Midtrans (bukan oleh user/browser),
 * setiap kali status pembayaran berubah (pending -> settlement -> dst).
 *
 * URL endpoint ini harus didaftarkan di Midtrans Dashboard:
 * Settings -> Configuration -> Payment Notification URL
 * contoh: https://domainkamu.com/payment/notification
 *
 * Endpoint ini WAJIB permitAll() di Spring Security (lihat catatan
 * SecurityConfig di bawah), karena tidak ada user yang login saat
 * Midtrans memanggilnya.
 */
@RestController
@RequestMapping("/payment")
public class PaymentNotificationController {

    private final PaymentService paymentService;
    private final MidtransConfig midtransConfig;

    public PaymentNotificationController(PaymentService paymentService, MidtransConfig midtransConfig) {
        this.paymentService = paymentService;
        this.midtransConfig = midtransConfig;
    }

    @PostMapping("/notification")
    public ResponseEntity<String> notification(@RequestBody Map<String, Object> payload) {

        String orderId = String.valueOf(payload.get("order_id"));
        String statusCode = String.valueOf(payload.get("status_code"));
        String grossAmount = String.valueOf(payload.get("gross_amount"));
        String signatureKey = String.valueOf(payload.get("signature_key"));

        String expectedSignature = SignatureUtil.generate(
                orderId, statusCode, grossAmount, midtransConfig.getServerKey());

        if (!expectedSignature.equals(signatureKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("invalid signature");
        }

        paymentService.handleNotification(payload);

        return ResponseEntity.ok("OK");
    }
}