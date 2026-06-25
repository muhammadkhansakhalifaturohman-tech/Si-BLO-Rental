package com.siblo.rent.service;

import com.siblo.rent.dto.AdminStatsDTO;
import com.siblo.rent.entity.Booking.BookingStatus;
import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class AdminService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    public AdminService(BookingRepository bookingRepository, CourtRepository courtRepository,
                        TimeSlotRepository timeSlotRepository) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    public AdminStatsDTO getDashboardStats() {
        LocalDate today = LocalDate.now();
        Long revenue = bookingRepository.sumRevenueByDate(today);
        long activeBookings = bookingRepository.countByStatus(BookingStatus.ACTIVE);
        long totalCourts = courtRepository.count();
        long activeCourts = courtRepository.countByStatus(CourtStatus.ACTIVE);
        int capacityPercent = totalCourts > 0 ? (int) ((activeCourts * 100) / totalCourts) : 0;
        long openSlots = timeSlotRepository.countCourtsWithAvailableSlots(today);
        return new AdminStatsDTO(revenue, "12% from yesterday", activeBookings,
            capacityPercent, openSlots, "2:00 PM");
    }
}
