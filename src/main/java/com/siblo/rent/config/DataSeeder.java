package com.siblo.rent.config;

import com.siblo.rent.entity.*;
import com.siblo.rent.entity.Booking.BookingStatus;
import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.entity.Payment.PaymentStatus;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.repository.*;
import com.siblo.rent.service.CourtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourtService courtService;

    @Value("${seed.enabled:true}")
    private boolean seedEnabled;

    public DataSeeder(UserRepository userRepository, SportRepository sportRepository,
                      VenueRepository venueRepository, CourtRepository courtRepository,
                      TimeSlotRepository timeSlotRepository, BookingRepository bookingRepository,
                      PaymentRepository paymentRepository, PasswordEncoder passwordEncoder,
                      CourtService courtService) {
        this.userRepository = userRepository;
        this.sportRepository = sportRepository;
        this.venueRepository = venueRepository;
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
        this.courtService = courtService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) return;
        if (userRepository.count() > 0) return;

        User admin = User.builder().name("Admin User").email("admin@siblo.com")
            .password(passwordEncoder.encode("admin123")).role(User.Role.ADMIN)
            .membershipTier("System Master").build();
        userRepository.save(admin);

        User member = User.builder().name("John Doe").email("john@siblo.com")
            .password(passwordEncoder.encode("john123")).role(User.Role.MEMBER)
            .membershipTier("PREMIUM MEMBER").build();
        userRepository.save(member);

        Sport basketball = sportRepository.save(Sport.builder().name("Basketball").slug("basketball").icon("🏀").locationCount(12).build());
        Sport futsal = sportRepository.save(Sport.builder().name("Futsal").slug("futsal").icon("⚽").locationCount(8).build());
        Sport padel = sportRepository.save(Sport.builder().name("Padel").slug("padel").icon("🎾").locationCount(15).build());
        Sport badminton = sportRepository.save(Sport.builder().name("Badminton").slug("badminton").icon("🏸").locationCount(6).build());
        Sport tennis = sportRepository.save(Sport.builder().name("Tennis").slug("tennis").icon("🎾").locationCount(4).build());

        Venue downtown = venueRepository.save(Venue.builder().name("Downtown Arena").address("123 Main St").zone("Central").latitude(-6.2088).longitude(106.8456).build());
        Venue glassHub = venueRepository.save(Venue.builder().name("Glass Hub").address("456 Park Ave").zone("Westside").latitude(-6.2250).longitude(106.8000).build());
        Venue eastSide = venueRepository.save(Venue.builder().name("East Side Complex").address("789 East Blvd").zone("Eastside").latitude(-6.2200).longitude(106.8800).build());
        Venue grandSlam = venueRepository.save(Venue.builder().name("Grand Slam Center").address("321 Sports Rd").zone("Central").latitude(-6.2400).longitude(106.8600).build());

        Court court1 = courtRepository.save(
    Court.builder()
        .venue(downtown)
        .sport(basketball)
        .name("Skyline Hoops Premium")
        .description("Premium basketball court with professional flooring and stadium lighting.")
        .surfaceType("Hardwood")
        .indoor(true)
        .pricePerHour(199000)
        .capacity(10)
        .rating(4.9)
        .reviewCount(128)
        .status(CourtStatus.ACTIVE)
        .badgeLabel("AVAILABLE")
        .imageUrl("/images/skyline-Hoops.png")
        .build()
);
       courtRepository.save(Court.builder().venue(glassHub).sport(padel)
    .name("Velocity Padel Center")
    .description("Professional padel court with synthetic turf and glass walls.")
    .surfaceType("Synthetic")
    .indoor(true)
    .pricePerHour(260000)
    .capacity(4)
    .rating(5.0)
    .reviewCount(89)
    .status(CourtStatus.ACTIVE)
    .badgeLabel("TOP RATED")
    .imageUrl("/images/velocity-padelCenter.png")
    .paymentLinkUrl("https://app.sandbox.midtrans.com/payment-links/414db0fa-bb87-44f8-a891-0cd9f6c57838-NhDpaERV")
    .build());

       courtRepository.save(Court.builder().venue(eastSide).sport(futsal)
    .name("Striker Futsal Indoor")
    .description("Indoor futsal court with high-quality artificial grass.")
    .surfaceType("Artificial Grass")
    .indoor(true)
    .pricePerHour(255000)
    .capacity(12)
    .rating(4.7)
    .reviewCount(215)
    .status(CourtStatus.ACTIVE)
    .badgeLabel("2 SLOTS LEFT")
    .imageUrl("/images/strikerFustalIndoor.png")
    .build());

       courtRepository.save(Court.builder().venue(grandSlam).sport(tennis)
    .name("Grand Slam Center - Court 04")
    .description("Premium professional acrylic surface with advanced shock absorption technology.")
    .surfaceType("Acrylic")
    .indoor(true)
    .pricePerHour(350000)
    .capacity(4)
    .rating(4.8)
    .reviewCount(56)
    .status(CourtStatus.ACTIVE)
    .badgeLabel("PREMIUM")
    .imageUrl("/images/grandSlamCourt04.png")
    .build());

        courtRepository.save(Court.builder().venue(downtown).sport(badminton)
    .name("Downtown Badminton Hall")
    .description("Professional badminton court with wooden flooring.")
    .surfaceType("Wooden")
    .indoor(true)
    .pricePerHour(150000)
    .capacity(4)
    .rating(4.5)
    .reviewCount(34)
    .status(CourtStatus.ACTIVE)
    .badgeLabel("AVAILABLE")
    .imageUrl("/images/downtownBadmintonHall.png")
    .build());

      courtRepository.save(Court.builder().venue(downtown).sport(futsal)
    .name("Premium SoccerField 73")
    .surfaceType("Hard Court")
    .indoor(true)
    .pricePerHour(255000)
    .capacity(10)
    .rating(4.5)
    .reviewCount(100)
    .status(CourtStatus.ACTIVE)
    .badgeLabel("PREMIUM")
    .imageUrl("/images/area73.png")
    .build());

        courtRepository.save(Court.builder().venue(eastSide).sport(basketball)
    .name("Lapangan Voli")
    .surfaceType("Clay")
    .indoor(false)
    .pricePerHour(150000)
    .capacity(12)
    .rating(4.2)
    .reviewCount(78)
    .status(CourtStatus.ACTIVE)
    .badgeLabel("AVAILABLE")
    .imageUrl("/images/Volypremium.png")
    .paymentLinkUrl("https://app.sandbox.midtrans.com/payment-links/46fe5a45-61ee-4f1f-8ce9-649a56c07be5-6O37zTNI")
    .build());

        List<Court> allCourts = courtRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Court court : allCourts) {
            courtService.generateSlots(court.getId(), 14);
        }

        // Seed bookings with proper slotIds and payments
        // Booking 1: COMPLETED - 2 days ago, 20:00-21:00, court1
        List<TimeSlot> completedSlots = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(court1.getId(), today.minusDays(2));
        TimeSlot completedSlot = completedSlots.stream()
            .filter(s -> s.getStartTime().equals(LocalTime.of(20, 0)))
            .findFirst().orElse(null);
        if (completedSlot != null) {
            completedSlot.setStatus(SlotStatus.BOOKED);
            timeSlotRepository.save(completedSlot);
            Booking b1 = new Booking();
            b1.setUser(member); b1.setCourt(court1);
            b1.setSlotIds(List.of(completedSlot.getId()));
            b1.setDate(today.minusDays(2));
            b1.setStartTime(LocalTime.of(20, 0));
            b1.setEndTime(LocalTime.of(21, 0));
            b1.setTotalPrice(199000);
            b1.setStatus(BookingStatus.COMPLETED);
            b1 = bookingRepository.save(b1);

            Payment p1 = new Payment();
            p1.setBooking(b1);
            p1.setAmount(199000);
            p1.setStatus(PaymentStatus.COMPLETED);
            p1.setMethod("SIMULATED");
            p1.setPaidAt(LocalDateTime.now().minusDays(1));
            paymentRepository.save(p1);
        }

        // Booking 2: COMPLETED - 5 days ago, 10:00-12:00, court1
        List<TimeSlot> completedSlots2 = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(court1.getId(), today.minusDays(5));
        List<TimeSlot> slot2a = completedSlots2.stream()
            .filter(s -> s.getStartTime().equals(LocalTime.of(10, 0))).findFirst().stream().toList();
        List<TimeSlot> slot2b = completedSlots2.stream()
            .filter(s -> s.getStartTime().equals(LocalTime.of(11, 0))).findFirst().stream().toList();
        List<TimeSlot> slots2 = new java.util.ArrayList<>();
        slots2.addAll(slot2a); slots2.addAll(slot2b);
        if (slots2.size() == 2) {
            for (TimeSlot s : slots2) s.setStatus(SlotStatus.BOOKED);
            timeSlotRepository.saveAll(slots2);
            Booking b2 = new Booking();
            b2.setUser(member); b2.setCourt(court1);
            b2.setSlotIds(slots2.stream().map(TimeSlot::getId).toList());
            b2.setDate(today.minusDays(5));
            b2.setStartTime(LocalTime.of(10, 0));
            b2.setEndTime(LocalTime.of(12, 0));
            b2.setTotalPrice(398000);
            b2.setStatus(BookingStatus.COMPLETED);
            b2 = bookingRepository.save(b2);

            Payment p2 = new Payment();
            p2.setBooking(b2);
            p2.setAmount(398000);
            p2.setStatus(PaymentStatus.COMPLETED);
            p2.setMethod("SIMULATED");
            p2.setPaidAt(LocalDateTime.now().minusDays(4));
            paymentRepository.save(p2);
        }

        // Booking 3: CONFIRMED - 2 days from now, 18:00-19:00, court1
        List<TimeSlot> confirmedSlots = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(court1.getId(), today.plusDays(2));
        TimeSlot confirmedSlot = confirmedSlots.stream()
            .filter(s -> s.getStartTime().equals(LocalTime.of(18, 0)))
            .findFirst().orElse(null);
        if (confirmedSlot != null) {
            confirmedSlot.setStatus(SlotStatus.BOOKED);
            timeSlotRepository.save(confirmedSlot);
            Booking b3 = new Booking();
            b3.setUser(member); b3.setCourt(court1);
            b3.setSlotIds(List.of(confirmedSlot.getId()));
            b3.setDate(today.plusDays(2));
            b3.setStartTime(LocalTime.of(18, 0));
            b3.setEndTime(LocalTime.of(19, 0));
            b3.setTotalPrice(199000);
            b3.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(b3);
        }

        // Booking 4: PENDING_PAYMENT - 1 day from now, 14:00-16:00, court1
        List<TimeSlot> pendingSlots = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(court1.getId(), today.plusDays(1));
        List<TimeSlot> slot4a = pendingSlots.stream()
            .filter(s -> s.getStartTime().equals(LocalTime.of(14, 0))).findFirst().stream().toList();
        List<TimeSlot> slot4b = pendingSlots.stream()
            .filter(s -> s.getStartTime().equals(LocalTime.of(15, 0))).findFirst().stream().toList();
        List<TimeSlot> slots4 = new java.util.ArrayList<>();
        slots4.addAll(slot4a); slots4.addAll(slot4b);
        if (slots4.size() == 2) {
            for (TimeSlot s : slots4) s.setStatus(SlotStatus.HELD);
            timeSlotRepository.saveAll(slots4);
            Booking b4 = new Booking();
            b4.setUser(member); b4.setCourt(court1);
            b4.setSlotIds(slots4.stream().map(TimeSlot::getId).toList());
            b4.setDate(today.plusDays(1));
            b4.setStartTime(LocalTime.of(14, 0));
            b4.setEndTime(LocalTime.of(16, 0));
            b4.setTotalPrice(398000);
            b4.setStatus(BookingStatus.PENDING_PAYMENT);
            b4.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(15));
            bookingRepository.save(b4);
        }
    }
}
