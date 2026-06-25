package com.siblo.rent.dto;

import com.siblo.rent.entity.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingDTO {
    private Long id; private Long userId; private String userName; private Long courtId;
    private String courtName; private String courtImageUrl; private String venueName;
    private LocalDate date; private String startTime; private String endTime;
    private Integer totalPrice; private String status; private LocalDateTime createdAt;
    private LocalDateTime paymentExpiresAt;

    public BookingDTO() {}

    public static BookingDTO fromEntity(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser() != null ? booking.getUser().getId() : null);
        dto.setUserName(booking.getUser() != null ? booking.getUser().getName() : null);
        dto.setCourtId(booking.getCourt() != null ? booking.getCourt().getId() : null);
        dto.setCourtName(booking.getCourt() != null ? booking.getCourt().getName() : null);
        dto.setCourtImageUrl(booking.getCourt() != null ? booking.getCourt().getImageUrl() : null);
        dto.setVenueName(booking.getCourt() != null && booking.getCourt().getVenue() != null
            ? booking.getCourt().getVenue().getName() : null);
        dto.setDate(booking.getDate());
        dto.setStartTime(booking.getStartTime() != null ? booking.getStartTime().toString() : null);
        dto.setEndTime(booking.getEndTime() != null ? booking.getEndTime().toString() : null);
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setPaymentExpiresAt(booking.getPaymentExpiresAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getCourtId() { return courtId; }
    public void setCourtId(Long courtId) { this.courtId = courtId; }
    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public String getCourtImageUrl() { return courtImageUrl; }
    public void setCourtImageUrl(String courtImageUrl) { this.courtImageUrl = courtImageUrl; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPaymentExpiresAt() { return paymentExpiresAt; }
    public void setPaymentExpiresAt(LocalDateTime paymentExpiresAt) { this.paymentExpiresAt = paymentExpiresAt; }
}
