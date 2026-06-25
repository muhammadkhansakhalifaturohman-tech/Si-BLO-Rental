package com.siblo.rent.controller;

import com.midtrans.httpclient.error.MidtransError;
import com.siblo.rent.config.MidtransConfig;
import com.siblo.rent.service.PaymentService;
import com.siblo.rent.entity.Booking;
import com.siblo.rent.entity.User;
import com.siblo.rent.exception.ResourceNotFoundException;
import com.siblo.rent.exception.UnauthorizedException;
import com.siblo.rent.repository.BookingRepository;
import com.siblo.rent.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final MidtransConfig midtransConfig;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public PaymentController(PaymentService paymentService, MidtransConfig midtransConfig,
                              BookingRepository bookingRepository, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.midtransConfig = midtransConfig;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/charge/{bookingId}")
    public ResponseEntity<?> charge(@PathVariable Long bookingId, Authentication auth) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!booking.getUser().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedException("Not authorized to pay this booking");
        }
        try {
            Map<String, String> result = paymentService.createPaymentForBooking(bookingId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (MidtransError e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Gagal membuat transaksi Midtrans: " + e.getMessage()));
        }
    }

    @GetMapping("/client-key")
    public ResponseEntity<Map<String, String>> getClientKey() {
        return ResponseEntity.ok(Map.of("clientKey", midtransConfig.getClientKey()));
    }

    @GetMapping("/snap-config")
    public ResponseEntity<Map<String, Object>> getSnapConfig() {
        String snapBaseUrl = midtransConfig.isProduction()
                ? "https://app.midtrans.com/snap/snap.js"
                : "https://app.sandbox.midtrans.com/snap/snap.js";
        return ResponseEntity.ok(Map.of(
            "clientKey", midtransConfig.getClientKey(),
            "isProduction", midtransConfig.isProduction(),
            "snapBaseUrl", snapBaseUrl
        ));
    }
}