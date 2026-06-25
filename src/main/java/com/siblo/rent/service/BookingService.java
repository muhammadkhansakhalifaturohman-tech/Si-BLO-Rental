package com.siblo.rent.service;

import com.siblo.rent.dto.BookingDTO;
import com.siblo.rent.dto.BookingRequest;
import com.siblo.rent.dto.BookingUpdateRequest;
import com.siblo.rent.entity.*;
import com.siblo.rent.entity.Booking.BookingStatus;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.exception.BookingException;
import com.siblo.rent.exception.ResourceNotFoundException;
import com.siblo.rent.exception.SlotNotAvailableException;
import com.siblo.rent.exception.UnauthorizedException;
import com.siblo.rent.repository.*;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final BookingExpiryService bookingExpiryService;

    @Value("${app.booking.payment-timeout-minutes:15}")
    private int paymentTimeoutMinutes;

    @Value("${app.booking.cancel-cutoff-hours:24}")
    private int cancelCutoffHours;

    public BookingService(BookingRepository bookingRepository, CourtRepository courtRepository,
                          TimeSlotRepository timeSlotRepository, UserRepository userRepository,
                          PaymentService paymentService, BookingExpiryService bookingExpiryService) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.bookingExpiryService = bookingExpiryService;
    }

    public BookingDTO getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return BookingDTO.fromEntity(booking);
    }

    public List<BookingDTO> getUserBookings(Long userId) {
        return bookingRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
            .map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    public List<BookingDTO> getUpcomingBookings(Long userId) {
        return bookingRepository.findUpcoming(userId,
            List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT, BookingStatus.ACTIVE),
            LocalDate.now()).stream()
            .map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    public List<BookingDTO> getPastBookings(Long userId) {
        return bookingRepository.findPast(userId, LocalDate.now()).stream()
            .map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO createBooking(BookingRequest request, Long userId) {
        if (request.getCourtId() == null || request.getSlotIds() == null || request.getSlotIds().isEmpty() || request.getDate() == null)
            throw new BookingException("courtId, slotIds, and date are required");

        LocalDate date = LocalDate.parse(request.getDate());
        if (date.isBefore(LocalDate.now()))
            throw new BookingException("Cannot book a past date");

        Court court = courtRepository.findById(request.getCourtId())
            .orElseThrow(() -> new ResourceNotFoundException("Court not found"));
        if (court.getStatus() != Court.CourtStatus.ACTIVE)
            throw new BookingException("Court is not available");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        bookingExpiryService.expirePendingPaymentsForCourtAndDate(request.getCourtId(), date);

        List<TimeSlot> slots;
        try {
            slots = timeSlotRepository.findAllByIdForUpdate(request.getSlotIds());
        } catch (OptimisticLockException e) {
            throw new SlotNotAvailableException("One or more slots were just booked");
        }

        if (slots.size() != request.getSlotIds().size())
            throw new BookingException("One or more slots not found");

        for (TimeSlot slot : slots) {
            if (!slot.getCourt().getId().equals(request.getCourtId()))
                throw new BookingException("Slot " + slot.getId() + " does not belong to the selected court");
            if (!slot.getDate().equals(date))
                throw new BookingException("Slot " + slot.getId() + " does not match the selected date");
            if (slot.getStatus() != SlotStatus.AVAILABLE)
                throw new SlotNotAvailableException("Slot " + slot.getId() + " is no longer available");
        }

        slots.sort(Comparator.comparing(TimeSlot::getStartTime));

        for (int i = 1; i < slots.size(); i++) {
            if (!slots.get(i - 1).getEndTime().equals(slots.get(i).getStartTime()))
                throw new BookingException("Slots must be consecutive");
        }

        LocalTime startTime = slots.get(0).getStartTime();
        LocalTime endTime = slots.get(slots.size() - 1).getEndTime();
        int totalPrice = slots.size() * court.getPricePerHour();

        for (TimeSlot slot : slots) {
            slot.setStatus(SlotStatus.HELD);
        }
        timeSlotRepository.saveAll(slots);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCourt(court);
        booking.setSlotIds(request.getSlotIds());
        booking.setDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(paymentTimeoutMinutes));
        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDTO payBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getId().equals(userId))
            throw new UnauthorizedException("Not authorized to pay this booking");

        // Already confirmed (e.g. by Midtrans webhook) — just return
        if (booking.getStatus() == BookingStatus.CONFIRMED)
            return BookingDTO.fromEntity(booking);

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT)
            throw new BookingException("Booking is not pending payment");
        if (booking.getPaymentExpiresAt() != null && booking.getPaymentExpiresAt().isBefore(LocalDateTime.now()))
            throw new BookingException("Payment window expired");

        List<TimeSlot> slots = timeSlotRepository.findAllById(booking.getSlotIds());
        for (TimeSlot slot : slots) {
            if (slot.getStatus() != SlotStatus.HELD)
                throw new SlotNotAvailableException("Slot " + slot.getId() + " is no longer held");
            slot.setStatus(SlotStatus.BOOKED);
        }
        timeSlotRepository.saveAll(slots);

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        return BookingDTO.fromEntity(booking);
    }

    @Transactional
    public BookingDTO cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getId().equals(userId))
            throw new UnauthorizedException("Not authorized to cancel this booking");

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.ACTIVE)
            throw new BookingException("Cannot cancel a " + booking.getStatus().name().toLowerCase() + " booking");

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            LocalDateTime startDateTime = booking.getDate().atTime(booking.getStartTime());
            if (startDateTime.isBefore(LocalDateTime.now().plusHours(cancelCutoffHours)))
                throw new BookingException("Cannot cancel within " + cancelCutoffHours + " hours of start time");
        }

        releaseSlots(booking.getSlotIds());

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            paymentService.refundPayment(booking);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDTO rescheduleBooking(Long bookingId, BookingUpdateRequest request, Long userId) {
        Booking oldBooking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!oldBooking.getUser().getId().equals(userId))
            throw new UnauthorizedException("Not authorized to reschedule this booking");

        if (oldBooking.getStatus() != BookingStatus.CONFIRMED
                && oldBooking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BookingException("Only confirmed or pending bookings can be rescheduled");
        }

        releaseSlots(oldBooking.getSlotIds());

        BookingRequest newRequest = new BookingRequest();
        newRequest.setCourtId(request.getCourtId() != null ? request.getCourtId() : oldBooking.getCourt().getId());
        newRequest.setSlotIds(request.getSlotIds());
        newRequest.setDate(request.getDate());

        boolean wasConfirmed = oldBooking.getStatus() == BookingStatus.CONFIRMED;

        oldBooking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(oldBooking);

        BookingDTO newBooking = createBooking(newRequest, userId);

        if (wasConfirmed) {
            Booking booking = bookingRepository.findById(newBooking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("New booking not found"));
            booking.setStatus(BookingStatus.CONFIRMED);
            List<TimeSlot> slots = timeSlotRepository.findAllById(booking.getSlotIds());
            for (TimeSlot slot : slots) {
                slot.setStatus(SlotStatus.BOOKED);
            }
            timeSlotRepository.saveAll(slots);
            return BookingDTO.fromEntity(bookingRepository.save(booking));
        }

        return newBooking;
    }

    private void releaseSlots(List<Long> slotIds) {
        List<TimeSlot> slots = timeSlotRepository.findAllById(slotIds);
        for (TimeSlot slot : slots) {
            slot.setStatus(SlotStatus.AVAILABLE);
        }
        timeSlotRepository.saveAll(slots);
    }

    public List<BookingDTO> getTimeline(LocalDate date) {
        return bookingRepository.findByDateOrderByStartTime(date).stream()
            .map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO adminConfirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT)
            throw new BookingException("Booking is not pending payment");
        if (booking.getPaymentExpiresAt() != null && booking.getPaymentExpiresAt().isBefore(LocalDateTime.now()))
            throw new BookingException("Payment window expired");

        List<TimeSlot> slots = timeSlotRepository.findAllById(booking.getSlotIds());
        for (TimeSlot slot : slots) {
            if (slot.getStatus() != SlotStatus.HELD)
                throw new SlotNotAvailableException("Slot " + slot.getId() + " is no longer held");
            slot.setStatus(SlotStatus.BOOKED);
        }
        timeSlotRepository.saveAll(slots);

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        return BookingDTO.fromEntity(booking);
    }
}
