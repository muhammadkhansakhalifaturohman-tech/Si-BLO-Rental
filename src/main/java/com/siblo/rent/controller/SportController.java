package com.siblo.rent.controller;

import com.siblo.rent.dto.SportDTO;
import com.siblo.rent.service.SportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sports")
public class SportController {

    private final SportService sportService;

    public SportController(SportService sportService) { this.sportService = sportService; }

    @GetMapping
    public ResponseEntity<List<SportDTO>> getAllSports() {
        return ResponseEntity.ok(sportService.getAllSports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SportDTO> getSport(@PathVariable Long id) {
        return ResponseEntity.ok(sportService.getSportById(id));
    }
}
