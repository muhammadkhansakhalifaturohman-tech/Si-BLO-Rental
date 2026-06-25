package com.siblo.rent.service;

import com.siblo.rent.dto.SportDTO;
import com.siblo.rent.repository.SportRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SportService {

    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) { this.sportRepository = sportRepository; }

    public List<SportDTO> getAllSports() {
        return sportRepository.findAll().stream().map(SportDTO::fromEntity).collect(Collectors.toList());
    }

    public SportDTO getSportById(Long id) {
        return sportRepository.findById(id).map(SportDTO::fromEntity)
            .orElseThrow(() -> new RuntimeException("Sport not found"));
    }
}
