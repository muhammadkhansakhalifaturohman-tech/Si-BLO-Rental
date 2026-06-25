package com.siblo.rent.controller;

import com.siblo.rent.dto.*;
import com.siblo.rent.repository.TimeSlotRepository;
import com.siblo.rent.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CourtService courtService;
    private final BookingService bookingService;
    private final TimeSlotRepository timeSlotRepository;

    public AdminController(AdminService adminService, CourtService courtService, BookingService bookingService, TimeSlotRepository timeSlotRepository) {
        this.adminService = adminService;
        this.courtService = courtService;
        this.bookingService = bookingService;
        this.timeSlotRepository = timeSlotRepository;
    }

    @GetMapping("/stats/dashboard")
    public ResponseEntity<AdminStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/courts")
    public ResponseEntity<List<CourtDTO>> getCourts() {
        return ResponseEntity.ok(courtService.getAllCourtsForAdmin());
    }

    @PostMapping("/courts")
    public ResponseEntity<CourtDTO> addCourt(@RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.addCourt(request));
    }

    @PutMapping("/courts/{id}")
    public ResponseEntity<CourtDTO> updateCourt(@PathVariable Long id, @RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.updateCourt(id, request));
    }

    @PatchMapping("/courts/{id}/availability")
    public ResponseEntity<Void> toggleAvailability(@PathVariable Long id) {
        courtService.toggleAvailability(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/courts/{id}")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/courts/{id}/slots")
    public ResponseEntity<Void> generateSlots(@PathVariable Long id, @RequestParam(defaultValue = "14") int days) {
        courtService.generateSlots(id, days);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/courts/{id}/slots/count")
    public ResponseEntity<Map<String, Object>> getSlotCount(@PathVariable Long id) {
        CourtDTO court = courtService.getCourtById(id);
        List<java.time.LocalDate> dates = timeSlotRepository.findByCourtIdAndDateAfter(id, java.time.LocalDate.now())
            .stream().map(ts -> ts.getDate()).distinct().sorted().collect(Collectors.toList());
        long count = timeSlotRepository.countByCourtIdAndDateAfter(id, java.time.LocalDate.now());
        return ResponseEntity.ok(Map.of(
            "count", count,
            "dates", dates.stream().map(d -> d.format(DateTimeFormatter.ofPattern("EEE, MMM d"))).collect(Collectors.toList()),
            "message", count > 0 ? "Slots exist for " + dates.size() + " upcoming days." : "No slots generated yet. Click generate to create slots."
        ));
    }

    @GetMapping("/bookings/timeline")
    public ResponseEntity<List<BookingDTO>> getTimeline(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(bookingService.getTimeline(date));
    }

    @GetMapping("/bookings/all")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PostMapping("/bookings/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.adminConfirmBooking(id));
    }
}
