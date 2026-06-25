package com.siblo.rent.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Util untuk verifikasi signature_key yang dikirim Midtrans di setiap
 * notification webhook.
 *
 * Rumus resmi Midtrans:
 *   signature_key = SHA512(order_id + status_code + gross_amount + server_key)
 *
 * Wajib diverifikasi sebelum mempercayai isi notifikasi, supaya tidak ada
 * pihak luar yang bisa kirim POST palsu ke /payment/notification dan
 * "melunasi" booking tanpa benar-benar bayar.
 */
public final class SignatureUtil {

    private SignatureUtil() {
    }

    public static String generate(String orderId, String statusCode, String grossAmount, String serverKey) {
        try {
            String input = orderId + statusCode + grossAmount + serverKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritma SHA-512 tidak tersedia", e);
        }
    }
}