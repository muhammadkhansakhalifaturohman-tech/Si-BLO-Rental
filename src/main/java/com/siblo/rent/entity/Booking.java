package com.siblo.rent.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @ElementCollection
    @CollectionTable(name = "booking_slot_ids", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "slot_id")
    private List<Long> slotIds = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "payment_expires_at")
    private LocalDateTime paymentExpiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum BookingStatus { CONFIRMED, PENDING_PAYMENT, COMPLETED, CANCELLED, ACTIVE }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Booking() {}

    public Booking(Long id, User user, Court court, List<Long> slotIds, LocalDate date,
                   LocalTime startTime, LocalTime endTime, Integer totalPrice, BookingStatus status) {
        this.id = id; this.user = user; this.court = court; this.slotIds = slotIds;
        this.date = date; this.startTime = startTime; this.endTime = endTime;
        this.totalPrice = totalPrice; this.status = status;
    }

    public static BookingBuilder builder() { return new BookingBuilder(); }

    public static class BookingBuilder {
        private Long id; private User user; private Court court; private List<Long> slotIds = new ArrayList<>();
        private LocalDate date; private LocalTime startTime; private LocalTime endTime;
        private Integer totalPrice; private BookingStatus status;
        BookingBuilder() {}
        public BookingBuilder id(Long id) { this.id = id; return this; }
        public BookingBuilder user(User user) { this.user = user; return this; }
        public BookingBuilder court(Court court) { this.court = court; return this; }
        public BookingBuilder slotIds(List<Long> slotIds) { this.slotIds = slotIds; return this; }
        public BookingBuilder date(LocalDate date) { this.date = date; return this; }
        public BookingBuilder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public BookingBuilder endTime(LocalTime endTime) { this.endTime = endTime; return this; }
        public BookingBuilder totalPrice(Integer totalPrice) { this.totalPrice = totalPrice; return this; }
        public BookingBuilder status(BookingStatus status) { this.status = status; return this; }
        public Booking build() { return new Booking(id, user, court, slotIds, date, startTime, endTime, totalPrice, status); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Court getCourt() { return court; }
    public void setCourt(Court court) { this.court = court; }
    public List<Long> getSlotIds() { return slotIds; }
    public void setSlotIds(List<Long> slotIds) { this.slotIds = slotIds; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Integer getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public LocalDateTime getPaymentExpiresAt() { return paymentExpiresAt; }
    public void setPaymentExpiresAt(LocalDateTime paymentExpiresAt) { this.paymentExpiresAt = paymentExpiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
