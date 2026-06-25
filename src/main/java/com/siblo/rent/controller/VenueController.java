package com.siblo.rent.controller;

import com.siblo.rent.dto.VenueDTO;
import com.siblo.rent.repository.VenueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueRepository venueRepository;

    public VenueController(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @GetMapping
    public ResponseEntity<List<VenueDTO>> getAllVenues() {
        return ResponseEntity.ok(venueRepository.findAllWithCourts().stream()
            .map(VenueDTO::fromEntity).collect(Collectors.toList()));
    }
}
