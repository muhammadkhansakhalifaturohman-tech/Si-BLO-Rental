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
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingLifecycleServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock TimeSlotRepository timeSlotRepository;
    @InjectMocks BookingLifecycleService service;

    private Booking booking(LocalDate date, LocalTime start, LocalTime end, BookingStatus status) {
        Booking b = new Booking();
        b.setId(1L);
        b.setDate(date);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setStatus(status);
        b.setSlotIds(List.of(10L, 11L));
        return b;
    }

    private TimeSlot slot(Long id, SlotStatus status) {
        TimeSlot s = new TimeSlot();
        s.setId(id);
        s.setStatus(status);
        return s;
    }

    @Test
    void confirmedToday_afterEndTime_becomesCompleted() {
        LocalDate today = LocalDate.now();
        Booking b = booking(today, LocalTime.of(18, 0), LocalTime.of(19, 0), BookingStatus.CONFIRMED);
        when(bookingRepository.findConfirmedReadyToComplete(any(), any())).thenReturn(List.of(b));
        when(bookingRepository.findConfirmedReadyToActivate(any(), any())).thenReturn(List.of());
        when(bookingRepository.findActiveReadyToComplete(any(), any())).thenReturn(List.of());
        when(timeSlotRepository.findAllById(anyList())).thenReturn(List.of(slot(10L, SlotStatus.BOOKED), slot(11L, SlotStatus.BOOKED)));

        service.advanceLifecycle();

        assertEquals(BookingStatus.COMPLETED, b.getStatus());
        verify(timeSlotRepository).saveAll(argThat(slots ->
            ((List<TimeSlot>) slots).stream().allMatch(s -> s.getStatus() == SlotStatus.AVAILABLE)));
    }

    @Test
    void confirmedToday_duringSession_becomesActive() {
        LocalDate today = LocalDate.now();
        Booking b = booking(today, LocalTime.of(18, 0), LocalTime.of(19, 0), BookingStatus.CONFIRMED);
        when(bookingRepository.findConfirmedReadyToComplete(any(), any())).thenReturn(List.of());
        when(bookingRepository.findConfirmedReadyToActivate(any(), any())).thenReturn(List.of(b));
        when(bookingRepository.findActiveReadyToComplete(any(), any())).thenReturn(List.of());

        service.advanceLifecycle();

        assertEquals(BookingStatus.ACTIVE, b.getStatus());
        verify(timeSlotRepository, never()).saveAll(anyList());
    }

    @Test
    void activeToday_afterEndTime_becomesCompleted() {
        LocalDate today = LocalDate.now();
        Booking b = booking(today, LocalTime.of(18, 0), LocalTime.of(19, 0), BookingStatus.ACTIVE);
        when(bookingRepository.findConfirmedReadyToComplete(any(), any())).thenReturn(List.of());
        when(bookingRepository.findConfirmedReadyToActivate(any(), any())).thenReturn(List.of());
        when(bookingRepository.findActiveReadyToComplete(any(), any())).thenReturn(List.of(b));
        when(timeSlotRepository.findAllById(anyList())).thenReturn(List.of(slot(10L, SlotStatus.BOOKED)));

        service.advanceLifecycle();

        assertEquals(BookingStatus.COMPLETED, b.getStatus());
        verify(timeSlotRepository).saveAll(argThat(slots ->
            ((List<TimeSlot>) slots).stream().allMatch(s -> s.getStatus() == SlotStatus.AVAILABLE)));
    }

    @Test
    void confirmedPastDate_becomesCompleted() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Booking b = booking(yesterday, LocalTime.of(10, 0), LocalTime.of(11, 0), BookingStatus.CONFIRMED);
        when(bookingRepository.findConfirmedReadyToComplete(any(), any())).thenReturn(List.of(b));
        when(bookingRepository.findConfirmedReadyToActivate(any(), any())).thenReturn(List.of());
        when(bookingRepository.findActiveReadyToComplete(any(), any())).thenReturn(List.of());
        when(timeSlotRepository.findAllById(anyList())).thenReturn(List.of(slot(10L, SlotStatus.BOOKED)));

        service.advanceLifecycle();

        assertEquals(BookingStatus.COMPLETED, b.getStatus());
    }

    @Test
    void noEligibleBookings_doesNothing() {
        when(bookingRepository.findConfirmedReadyToComplete(any(), any())).thenReturn(List.of());
        when(bookingRepository.findConfirmedReadyToActivate(any(), any())).thenReturn(List.of());
        when(bookingRepository.findActiveReadyToComplete(any(), any())).thenReturn(List.of());

        int count = service.advanceLifecycle();

        assertEquals(0, count);
        verify(bookingRepository, never()).saveAll(anyList());
        verify(timeSlotRepository, never()).saveAll(anyList());
    }
}
