package com.siblo.rent.controller;

import com.siblo.rent.dto.CourtDTO;
import com.siblo.rent.dto.TimeSlotDTO;
import com.siblo.rent.service.CourtService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courts")
public class CourtController {

    private final CourtService courtService;

    public CourtController(CourtService courtService) { this.courtService = courtService; }

    @GetMapping
    public ResponseEntity<List<CourtDTO>> getCourts(
            @RequestParam(required = false) Long sport,
            @RequestParam(required = false) String search) {
        if (search != null && !search.isEmpty())
            return ResponseEntity.ok(courtService.searchCourts(search));
        return ResponseEntity.ok(courtService.getActiveCourts(sport));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getAvailableCount() {
        return ResponseEntity.ok(Map.of("count", courtService.getAvailableCourtsCount()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourtDTO> getCourt(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<TimeSlotDTO>> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(courtService.getAvailability(id, date));
    }
}
