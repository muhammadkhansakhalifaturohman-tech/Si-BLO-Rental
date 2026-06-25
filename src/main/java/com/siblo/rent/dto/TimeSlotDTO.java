package com.siblo.rent.dto;

import com.siblo.rent.entity.TimeSlot;
import java.time.LocalTime;

public class TimeSlotDTO {
    private Long id; private LocalTime startTime; private LocalTime endTime; private String status;

    public TimeSlotDTO() {}

    public static TimeSlotDTO fromEntity(TimeSlot slot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(slot.getId()); dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setStatus(slot.getStatus() != null ? slot.getStatus().name() : null);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
