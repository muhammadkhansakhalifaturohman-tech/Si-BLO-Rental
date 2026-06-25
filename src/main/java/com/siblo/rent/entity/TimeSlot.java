package com.siblo.rent.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;

    @Version
    private Long version;

    public enum SlotStatus { AVAILABLE, HELD, BOOKED, BLOCKED }

    public TimeSlot() {}

    public TimeSlot(Long id, Court court, LocalDate date, LocalTime startTime, LocalTime endTime, SlotStatus status) {
        this.id = id; this.court = court; this.date = date;
        this.startTime = startTime; this.endTime = endTime; this.status = status;
    }

    public static TimeSlotBuilder builder() { return new TimeSlotBuilder(); }

    public static class TimeSlotBuilder {
        private Long id; private Court court; private LocalDate date;
        private LocalTime startTime; private LocalTime endTime; private SlotStatus status;
        TimeSlotBuilder() {}
        public TimeSlotBuilder id(Long id) { this.id = id; return this; }
        public TimeSlotBuilder court(Court court) { this.court = court; return this; }
        public TimeSlotBuilder date(LocalDate date) { this.date = date; return this; }
        public TimeSlotBuilder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public TimeSlotBuilder endTime(LocalTime endTime) { this.endTime = endTime; return this; }
        public TimeSlotBuilder status(SlotStatus status) { this.status = status; return this; }
        public TimeSlot build() { return new TimeSlot(id, court, date, startTime, endTime, status); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Court getCourt() { return court; }
    public void setCourt(Court court) { this.court = court; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
