package com.siblo.rent.service;

import com.siblo.rent.dto.BookingRequest;
import com.siblo.rent.entity.Booking;
import com.siblo.rent.entity.Booking.BookingStatus;
import com.siblo.rent.entity.Court;
import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.entity.TimeSlot;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.entity.User;
import com.siblo.rent.exception.BookingException;
import com.siblo.rent.exception.ResourceNotFoundException;
import com.siblo.rent.exception.SlotNotAvailableException;
import com.siblo.rent.exception.UnauthorizedException;
import com.siblo.rent.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock CourtRepository courtRepository;
    @Mock TimeSlotRepository timeSlotRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentService paymentService;
    @Mock BookingExpiryService bookingExpiryService;

    @InjectMocks BookingService bookingService;

    private Court activeCourt;
    private User user;
    private TimeSlot slot1, slot2;
    private List<TimeSlot> contiguousSlots;
    private BookingRequest validRequest;

    @BeforeEach
    void setUp() {
        activeCourt = new Court();
        activeCourt.setId(1L);
        activeCourt.setStatus(CourtStatus.ACTIVE);
        activeCourt.setPricePerHour(100000);

        user = new User();
        user.setId(1L);
        user.setName("Test User");

        slot1 = new TimeSlot();
        slot1.setId(10L);
        slot1.setCourt(activeCourt);
        slot1.setDate(LocalDate.now().plusDays(1));
        slot1.setStartTime(LocalTime.of(10, 0));
        slot1.setEndTime(LocalTime.of(11, 0));
        slot1.setStatus(SlotStatus.AVAILABLE);

        slot2 = new TimeSlot();
        slot2.setId(11L);
        slot2.setCourt(activeCourt);
        slot2.setDate(LocalDate.now().plusDays(1));
        slot2.setStartTime(LocalTime.of(11, 0));
        slot2.setEndTime(LocalTime.of(12, 0));
        slot2.setStatus(SlotStatus.AVAILABLE);

        contiguousSlots = new ArrayList<>(Arrays.asList(slot1, slot2));

        validRequest = new BookingRequest();
        validRequest.setCourtId(1L);
        validRequest.setSlotIds(new ArrayList<>(Arrays.asList(10L, 11L)));
        validRequest.setDate(LocalDate.now().plusDays(1).toString());
    }

    @Test
    void createBooking_nullCourtId_throwsBookingException() {
        BookingRequest req = new BookingRequest();
        req.setSlotIds(List.of(10L));
        req.setDate(LocalDate.now().plusDays(1).toString());

        assertThrows(BookingException.class, () -> bookingService.createBooking(req, 1L));
    }

    @Test
    void createBooking_emptySlotIds_throwsBookingException() {
        BookingRequest req = new BookingRequest();
        req.setCourtId(1L);
        req.setSlotIds(List.of());
        req.setDate(LocalDate.now().plusDays(1).toString());

        assertThrows(BookingException.class, () -> bookingService.createBooking(req, 1L));
    }

    @Test
    void createBooking_pastDate_throwsBookingException() {
        BookingRequest req = new BookingRequest();
        req.setCourtId(1L);
        req.setSlotIds(List.of(10L));
        req.setDate(LocalDate.now().minusDays(1).toString());

        assertThrows(BookingException.class, () -> bookingService.createBooking(req, 1L));
    }

    @Test
    void createBooking_courtNotFound_throwsResourceNotFoundException() {
        when(courtRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(validRequest, 1L));
    }

    @Test
    void createBooking_courtInactive_throwsBookingException() {
        activeCourt.setStatus(CourtStatus.INACTIVE);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(activeCourt));

        assertThrows(BookingException.class, () -> bookingService.createBooking(validRequest, 1L));
    }

    @Test
    void createBooking_slotNotFound_throwsBookingException() {
        when(courtRepository.findById(1L)).thenReturn(Optional.of(activeCourt));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(timeSlotRepository.findAllByIdForUpdate(anyList())).thenReturn(List.of(slot1));

        assertThrows(BookingException.class, () -> bookingService.createBooking(validRequest, 1L));
    }

    @Test
    void createBooking_wrongCourt_throwsBookingException() {
        Court otherCourt = new Court();
        otherCourt.setId(99L);
        slot1.setCourt(otherCourt);

        when(courtRepository.findById(1L)).thenReturn(Optional.of(activeCourt));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(timeSlotRepository.findAllByIdForUpdate(anyList())).thenReturn(List.of(slot1, slot2));

        assertThrows(BookingException.class, () -> bookingService.createBooking(validRequest, 1L));
    }

    @Test
    void createBooking_slotNotAvailable_throwsSlotNotAvailableException() {
        slot1.setStatus(SlotStatus.BOOKED);

        when(courtRepository.findById(1L)).thenReturn(Optional.of(activeCourt));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(timeSlotRepository.findAllByIdForUpdate(anyList())).thenReturn(new ArrayList<>(Arrays.asList(slot1, slot2)));

        assertThrows(SlotNotAvailableException.class, () -> bookingService.createBooking(validRequest, 1L));
    }

    @Test
    void createBooking_nonContiguousSlots_throwsBookingException() {
        slot2.setStartTime(LocalTime.of(13, 0));
        slot2.setEndTime(LocalTime.of(14, 0));

        when(courtRepository.findById(1L)).thenReturn(Optional.of(activeCourt));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(timeSlotRepository.findAllByIdForUpdate(anyList())).thenReturn(new ArrayList<>(Arrays.asList(slot1, slot2)));

        assertThrows(BookingException.class, () -> bookingService.createBooking(validRequest, 1L));
    }

    @Test
    void createBooking_validRequest_createsPendingPayment() {
        when(courtRepository.findById(1L)).thenReturn(Optional.of(activeCourt));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(timeSlotRepository.findAllByIdForUpdate(anyList())).thenReturn(contiguousSlots);
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        var dto = bookingService.createBooking(validRequest, 1L);

        assertNotNull(dto);
        assertEquals("PENDING_PAYMENT", dto.getStatus());
        assertEquals(Integer.valueOf(200000), dto.getTotalPrice());
        assertEquals(SlotStatus.HELD, slot1.getStatus());
        assertEquals(SlotStatus.HELD, slot2.getStatus());
        verify(timeSlotRepository).saveAll(contiguousSlots);
    }

    @Test
    void payBooking_wrongUser_throwsUnauthorizedException() {
        Booking booking = new Booking();
        booking.setId(1L);
        User otherUser = new User();
        otherUser.setId(99L);
        booking.setUser(otherUser);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class, () -> bookingService.payBooking(1L, 1L));
    }

    @Test
    void payBooking_alreadyConfirmed_returnsBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        var dto = bookingService.payBooking(1L, 1L);

        assertEquals("CONFIRMED", dto.getStatus());
        verify(paymentService, never()).completePayment(any());
    }

    @Test
    void payBooking_expired_throwsBookingException() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setPaymentExpiresAt(java.time.LocalDateTime.now().minusMinutes(1));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingException.class, () -> bookingService.payBooking(1L, 1L));
    }

    @Test
    void cancelBooking_wrongUser_throwsUnauthorizedException() {
        Booking booking = new Booking();
        booking.setId(1L);
        User otherUser = new User();
        otherUser.setId(99L);
        booking.setUser(otherUser);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class, () -> bookingService.cancelBooking(1L, 1L));
    }

    @Test
    void cancelBooking_completed_throwsBookingException() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingException.class, () -> bookingService.cancelBooking(1L, 1L));
    }

    @Test
    void cancelBooking_pendingPayment_cancelsAndReleasesSlots() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setSlotIds(List.of(10L, 11L));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(timeSlotRepository.findAllById(anyList())).thenReturn(List.of(slot1, slot2));
        when(bookingRepository.save(any())).thenReturn(booking);

        var dto = bookingService.cancelBooking(1L, 1L);

        assertEquals("CANCELLED", dto.getStatus());
        assertEquals(SlotStatus.AVAILABLE, slot1.getStatus());
        assertEquals(SlotStatus.AVAILABLE, slot2.getStatus());
        verify(paymentService, never()).refundPayment(any());
    }
}
