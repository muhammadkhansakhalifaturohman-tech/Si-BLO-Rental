package com.siblo.rent.controller;

import com.siblo.rent.dto.UserDTO;
import com.siblo.rent.entity.User;
import com.siblo.rent.entity.User.Role;
import com.siblo.rent.repository.UserRepository;
import com.siblo.rent.security.JwtTokenProvider;
import com.siblo.rent.service.EmailService;
import com.siblo.rent.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;

    private final Map<String, Map<String, String>> pendingRegistrations = new ConcurrentHashMap<>();

    public AuthController(AuthenticationManager authManager,
                           JwtTokenProvider tokenProvider,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           OtpService otpService,
                           EmailService emailService) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of(
            "token", token, "email", user.getEmail(),
            "name", user.getName(), "role", user.getRole().name(),
            "membershipTier", user.getMembershipTier()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (body.containsKey("name")) user.setName(body.get("name"));
        if (body.containsKey("email")) {
            String newEmail = body.get("email");
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail))
                return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
            user.setEmail(newEmail);
        }
        userRepository.save(user);
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(Authentication auth, @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * LANGKAH 1 REGISTER: Terima data user, kirim OTP ke email.
     * Akun belum dibuat sampai OTP diverifikasi.
     */
    @PostMapping("/register/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String name = body.get("name");
        String password = body.get("password");

        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$"))
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
        if (name == null || name.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        if (password == null || password.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));

        if (userRepository.existsByEmail(email))
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));

        pendingRegistrations.put(email, new ConcurrentHashMap<>(Map.of(
            "name", name,
            "email", email,
            "password", passwordEncoder.encode(password)
        )));

        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok(Map.of("message", "OTP sent to " + email));
    }

    /**
     * LANGKAH 2 REGISTER: Verifikasi OTP, baru buat akun.
     */
    @PostMapping("/register/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("otp");

        if (!otpService.verifyOtp(email, code))
            return ResponseEntity.badRequest().body(Map.of("error", "Kode OTP salah atau sudah expired"));

        Map<String, String> pending = pendingRegistrations.get(email);
        if (pending == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Data registrasi tidak ditemukan, silakan daftar ulang"));

        // Buat akun setelah OTP valid (password already hashed in sendOtp)
        User user = User.builder()
            .name(pending.get("name"))
            .email(email)
            .password(pending.get("password"))
            .role(Role.MEMBER)
            .membershipTier("PREMIUM MEMBER")
            .build();
        userRepository.save(user);
        pendingRegistrations.remove(email);

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "email", user.getEmail(),
            "name", user.getName(),
            "role", user.getRole().name()
        ));
    }

    /**
     * Legacy direct register endpoint — removed for security.
     * All registrations must go through OTP verification.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register() {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", "Direct registration is disabled. Use /register/send-otp and /register/verify-otp instead."));
    }
}
