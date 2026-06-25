package com.siblo.rent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "transaction_id")
    private String transactionId;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String method;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public enum PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }

    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
