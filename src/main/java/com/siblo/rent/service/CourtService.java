package com.siblo.rent.service;

import com.siblo.rent.dto.CourtDTO;
import com.siblo.rent.dto.CourtRequest;
import com.siblo.rent.dto.TimeSlotDTO;
import com.siblo.rent.entity.*;
import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.exception.ResourceNotFoundException;
import com.siblo.rent.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtService {

    private final CourtRepository courtRepository;
    private final SportRepository sportRepository;
    private final VenueRepository venueRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingExpiryService bookingExpiryService;

    @Value("${app.scheduling.slot-horizon-days:14}")
    private int slotHorizonDays;

    public CourtService(CourtRepository courtRepository, SportRepository sportRepository,
                        VenueRepository venueRepository, TimeSlotRepository timeSlotRepository,
                        BookingExpiryService bookingExpiryService) {
        this.courtRepository = courtRepository;
        this.sportRepository = sportRepository;
        this.venueRepository = venueRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingExpiryService = bookingExpiryService;
    }

    public List<CourtDTO> getActiveCourts(Long sportId) {
        List<Court> courts = sportId != null
            ? courtRepository.findActiveCourts(sportId)
            : courtRepository.findByStatus(CourtStatus.ACTIVE);
        return courts.stream().map(CourtDTO::fromEntity).collect(Collectors.toList());
    }

    public List<CourtDTO> searchCourts(String query) {
        return courtRepository.searchCourts(query).stream()
            .map(CourtDTO::fromEntity).collect(Collectors.toList());
    }

    public CourtDTO getCourtById(Long id) {
        return courtRepository.findById(id).map(CourtDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Court not found"));
    }

    @Transactional
    public List<TimeSlotDTO> getAvailability(Long courtId, LocalDate date) {
        List<TimeSlot> slots = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(courtId, date);
        if (slots.isEmpty()) {
            Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));
            if (court.getStatus() == CourtStatus.ACTIVE && !date.isBefore(LocalDate.now())) {
                generateSlots(courtId, slotHorizonDays);
                slots = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(courtId, date);
            }
        }

        bookingExpiryService.expirePendingPaymentsForCourtAndDate(courtId, date);

        return slots.stream().map(TimeSlotDTO::fromEntity).collect(Collectors.toList());
    }

    public long getAvailableCourtsCount() {
        return timeSlotRepository.countCourtsWithAvailableSlots(LocalDate.now());
    }

    @Transactional
    public CourtDTO addCourt(CourtRequest request) {
        Sport sport = sportRepository.findById(request.getSportId())
            .orElseThrow(() -> new RuntimeException("Sport not found"));
        Court court = new Court();
        court.setName(request.getName());
        court.setDescription(request.getDescription());
        court.setSurfaceType(request.getSurfaceType());
        court.setIndoor(request.getIndoor() != null ? request.getIndoor() : true);
        court.setPricePerHour(request.getPricePerHour());
        court.setCapacity(request.getCapacity());
        court.setRating(0.0);
        court.setReviewCount(0);
        court.setStatus(CourtStatus.ACTIVE);
        court.setSport(sport);
        court.setPaymentLinkUrl(request.getPaymentLinkUrl());
        court.setOpenTime(request.getOpenTime() != null ? request.getOpenTime() : LocalTime.of(6, 0));
        court.setCloseTime(request.getCloseTime() != null ? request.getCloseTime() : LocalTime.of(22, 0));
        if (request.getVenueId() != null) {
            venueRepository.findById(request.getVenueId()).ifPresent(v -> court.setVenue(v));
        }
        Court saved = courtRepository.save(court);
        generateSlots(saved.getId(), slotHorizonDays);
        return CourtDTO.fromEntity(saved);
    }

    @Transactional
    public CourtDTO updateCourt(Long id, CourtRequest request) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Court not found"));
        court.setName(request.getName());
        court.setDescription(request.getDescription());
        court.setSurfaceType(request.getSurfaceType());
        court.setIndoor(request.getIndoor() != null ? request.getIndoor() : court.getIndoor());
        court.setPricePerHour(request.getPricePerHour());
        court.setCapacity(request.getCapacity());
        if (request.getImageUrl() != null) court.setImageUrl(request.getImageUrl());
        court.setPaymentLinkUrl(request.getPaymentLinkUrl());
        if (request.getOpenTime() != null) court.setOpenTime(request.getOpenTime());
        if (request.getCloseTime() != null) court.setCloseTime(request.getCloseTime());
        if (request.getStatus() != null) {
            court.setStatus(CourtStatus.valueOf(request.getStatus()));
        }
        if (request.getSportId() != null) {
            court.setSport(sportRepository.findById(request.getSportId())
                .orElseThrow(() -> new RuntimeException("Sport not found")));
        }
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Transactional
    public void toggleAvailability(Long id) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Court not found"));
        court.setStatus(court.getStatus() == CourtStatus.ACTIVE ? CourtStatus.INACTIVE : CourtStatus.ACTIVE);
        courtRepository.save(court);
    }

    @Transactional
    public void deleteCourt(Long id) { courtRepository.deleteById(id); }

    public List<CourtDTO> getAllCourtsForAdmin() {
        return courtRepository.findAll().stream().map(CourtDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public void generateSlots(Long courtId, int days) {
        Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new RuntimeException("Court not found"));
        LocalTime open = court.getOpenTime() != null ? court.getOpenTime() : LocalTime.of(6, 0);
        LocalTime close = court.getCloseTime() != null ? court.getCloseTime() : LocalTime.of(22, 0);
        LocalDate today = LocalDate.now();

        LocalTime slotStart = open;
        if (open.getMinute() > 0 || open.getSecond() > 0) {
            slotStart = open.withMinute(0).withSecond(0).withNano(0).plusHours(1);
        }

        for (int day = 0; day < days; day++) {
            LocalDate date = today.plusDays(day);
            LocalTime current = slotStart;
            while (current.isBefore(close)) {
                LocalTime end = current.plusHours(1);
                if (end.isAfter(close)) break;
                if (!timeSlotRepository.existsByCourtIdAndDateAndStartTime(courtId, date, current)) {
                    timeSlotRepository.save(TimeSlot.builder().court(court).date(date)
                        .startTime(current).endTime(end)
                        .status(SlotStatus.AVAILABLE).build());
                }
                current = end;
            }
        }
    }
}
