package com.siblo.rent.dto;

import com.siblo.rent.entity.Payment;
import java.time.LocalDateTime;

public class PaymentDTO {
    private Long id;
    private Long bookingId;
    private Integer amount;
    private String status;
    private String method;
    private LocalDateTime paidAt;

    public PaymentDTO() {}

    public static PaymentDTO fromEntity(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setBookingId(payment.getBooking() != null ? payment.getBooking().getId() : null);
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setMethod(payment.getMethod());
        dto.setPaidAt(payment.getPaidAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
