package com.siblo.rent.service;

import com.siblo.rent.entity.Booking;
import com.siblo.rent.entity.Booking.BookingStatus;
import com.siblo.rent.entity.TimeSlot;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.repository.BookingRepository;
import com.siblo.rent.repository.TimeSlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingExpiryServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock TimeSlotRepository timeSlotRepository;
    @InjectMocks BookingExpiryService service;

    private Booking pendingBooking(Long id, LocalDateTime expiresAt, List<Long> slotIds) {
        Booking b = new Booking();
        b.setId(id);
        b.setStatus(BookingStatus.PENDING_PAYMENT);
        b.setPaymentExpiresAt(expiresAt);
        b.setSlotIds(slotIds);
        return b;
    }

    @Test
    void expirePendingPayments_releasesHeldSlotsAndCancels() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        Booking b1 = pendingBooking(1L, past, List.of(10L, 11L));
        when(bookingRepository.findByStatusAndPaymentExpiresAtBefore(eq(BookingStatus.PENDING_PAYMENT), any()))
            .thenReturn(List.of(b1));
        when(timeSlotRepository.findAllById(anyList()))
            .thenReturn(List.of(slot(10L, SlotStatus.HELD), slot(11L, SlotStatus.HELD)));

        int count = service.expirePendingPayments();

        assertEquals(1, count);
        assertEquals(BookingStatus.CANCELLED, b1.getStatus());
        verify(bookingRepository).saveAll(anyList());
        verify(timeSlotRepository).saveAll(argThat(slots ->
            ((List<TimeSlot>) slots).stream().allMatch(s -> s.getStatus() == SlotStatus.AVAILABLE)));
    }

    @Test
    void expirePendingPayments_noExpired_doesNothing() {
        when(bookingRepository.findByStatusAndPaymentExpiresAtBefore(eq(BookingStatus.PENDING_PAYMENT), any()))
            .thenReturn(List.of());

        int count = service.expirePendingPayments();

        assertEquals(0, count);
        verify(bookingRepository, never()).saveAll(anyList());
        verify(timeSlotRepository, never()).saveAll(anyList());
    }

    @Test
    void expireForCourtAndDate_onlyExpiredAreCancelled() {
        LocalDate today = LocalDate.now();
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        LocalDateTime future = LocalDateTime.now().plusMinutes(30);

        Booking expired = pendingBooking(1L, past, List.of(10L));
        Booking valid = pendingBooking(2L, future, List.of(20L));

        when(bookingRepository.findByCourtIdAndDate(1L, today)).thenReturn(List.of(expired, valid));
        when(timeSlotRepository.findAllById(anyList())).thenReturn(List.of(slot(10L, SlotStatus.HELD)));

        int count = service.expirePendingPaymentsForCourtAndDate(1L, today);

        assertEquals(1, count);
        assertEquals(BookingStatus.CANCELLED, expired.getStatus());
        assertEquals(BookingStatus.PENDING_PAYMENT, valid.getStatus());
    }

    private TimeSlot slot(Long id, SlotStatus status) {
        TimeSlot s = new TimeSlot();
        s.setId(id);
        s.setStatus(status);
        return s;
    }
}
