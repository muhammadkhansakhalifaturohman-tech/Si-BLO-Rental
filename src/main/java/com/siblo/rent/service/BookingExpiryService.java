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
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingExpiryService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;

    public BookingExpiryService(BookingRepository bookingRepository, TimeSlotRepository timeSlotRepository) {
        this.bookingRepository = bookingRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional
    public int expirePendingPayments() {
        List<Booking> expired = bookingRepository
            .findByStatusAndPaymentExpiresAtBefore(BookingStatus.PENDING_PAYMENT, LocalDateTime.now());
        for (Booking booking : expired) {
            releaseHeldSlots(booking);
            booking.setStatus(BookingStatus.CANCELLED);
        }
        if (!expired.isEmpty()) {
            bookingRepository.saveAll(expired);
        }
        return expired.size();
    }

    @Transactional
    public int expirePendingPaymentsForCourtAndDate(Long courtId, LocalDate date) {
        List<Booking> candidates = bookingRepository.findByCourtIdAndDate(courtId, date);
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (Booking booking : candidates) {
            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT
                    && booking.getPaymentExpiresAt() != null
                    && booking.getPaymentExpiresAt().isBefore(now)) {
                releaseHeldSlots(booking);
                booking.setStatus(BookingStatus.CANCELLED);
                count++;
            }
        }
        if (count > 0) {
            bookingRepository.saveAll(candidates);
        }
        return count;
    }

    private void releaseHeldSlots(Booking booking) {
        List<TimeSlot> slots = timeSlotRepository.findAllById(booking.getSlotIds());
        boolean changed = false;
        for (TimeSlot slot : slots) {
            if (slot.getStatus() == SlotStatus.HELD) {
                slot.setStatus(SlotStatus.AVAILABLE);
                changed = true;
            }
        }
        if (changed) {
            timeSlotRepository.saveAll(slots);
        }
    }
}
