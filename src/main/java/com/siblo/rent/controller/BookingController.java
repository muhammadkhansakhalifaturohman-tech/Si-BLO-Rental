package com.siblo.rent.controller;

import com.siblo.rent.dto.BookingDTO;
import com.siblo.rent.dto.BookingRequest;
import com.siblo.rent.dto.BookingUpdateRequest;
import com.siblo.rent.entity.User;
import com.siblo.rent.exception.BookingException;
import com.siblo.rent.exception.ResourceNotFoundException;
import com.siblo.rent.exception.UnauthorizedException;
import com.siblo.rent.repository.UserRepository;
import com.siblo.rent.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyBookings(Authentication auth,
            @RequestParam(required = false) Boolean upcoming,
            @RequestParam(required = false) Boolean past) {
        User user = getUser(auth);
        if (Boolean.TRUE.equals(upcoming))
            return ResponseEntity.ok(bookingService.getUpcomingBookings(user.getId()));
        if (Boolean.TRUE.equals(past))
            return ResponseEntity.ok(bookingService.getPastBookings(user.getId()));
        return ResponseEntity.ok(bookingService.getUserBookings(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request, Authentication auth) {
        User user = getUser(auth);
        return ResponseEntity.ok(bookingService.createBooking(request, user.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody BookingUpdateRequest body, Authentication auth) {
        User user = getUser(auth);
        if ("cancel".equals(body.getAction())) {
            return ResponseEntity.ok(bookingService.cancelBooking(id, user.getId()));
        } else if ("reschedule".equals(body.getAction())) {
            return ResponseEntity.ok(bookingService.rescheduleBooking(id, body, user.getId()));
        }
        throw new BookingException("Invalid action: " + body.getAction());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        BookingDTO booking = bookingService.getBookingById(id);
        if (!booking.getUserId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Not authorized to view this booking");
        }
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payBooking(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        return ResponseEntity.ok(bookingService.payBooking(id, user.getId()));
    }

    private User getUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            throw new UnauthorizedException("Authentication required");
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
