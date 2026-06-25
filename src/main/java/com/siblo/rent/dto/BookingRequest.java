package com.siblo.rent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BookingRequest {
    @NotNull
    private Long courtId;
    @NotEmpty
    private List<Long> slotIds;
    @NotBlank
    private String date;

    public Long getCourtId() { return courtId; }
    public void setCourtId(Long courtId) { this.courtId = courtId; }
    public List<Long> getSlotIds() { return slotIds; }
    public void setSlotIds(List<Long> slotIds) { this.slotIds = slotIds; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
