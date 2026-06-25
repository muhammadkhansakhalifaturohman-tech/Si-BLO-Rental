package com.siblo.rent.service;

import com.siblo.rent.entity.Booking;
import com.siblo.rent.entity.Booking.BookingStatus;
import com.siblo.rent.entity.TimeSlot;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.repository.BookingRepository;
import com.siblo.rent.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingLifecycleService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;

    public BookingLifecycleService(BookingRepository bookingRepository, TimeSlotRepository timeSlotRepository) {
        this.bookingRepository = bookingRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional
    public int advanceLifecycle() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        int count = 0;

        List<Booking> confirmToComplete = bookingRepository
            .findConfirmedReadyToComplete(today, now);
        for (Booking b : confirmToComplete) {
            b.setStatus(BookingStatus.COMPLETED);
            releaseSlots(b);
            count++;
        }

        List<Booking> confirmToActive = bookingRepository
            .findConfirmedReadyToActivate(today, now);
        for (Booking b : confirmToActive) {
            b.setStatus(BookingStatus.ACTIVE);
            count++;
        }

        List<Booking> activeToComplete = bookingRepository
            .findActiveReadyToComplete(today, now);
        for (Booking b : activeToComplete) {
            b.setStatus(BookingStatus.COMPLETED);
            releaseSlots(b);
            count++;
        }

        if (!confirmToComplete.isEmpty()) bookingRepository.saveAll(confirmToComplete);
        if (!confirmToActive.isEmpty()) bookingRepository.saveAll(confirmToActive);
        if (!activeToComplete.isEmpty()) bookingRepository.saveAll(activeToComplete);

        return count;
    }

    private void releaseSlots(Booking booking) {
        List<TimeSlot> slots = timeSlotRepository.findAllById(booking.getSlotIds());
        for (TimeSlot slot : slots) {
            slot.setStatus(SlotStatus.AVAILABLE);
        }
        timeSlotRepository.saveAll(slots);
    }
}
