package com.siblo.rent.dto;

import java.util.List;

public class BookingUpdateRequest {
    private String action;
    private Long courtId;
    private List<Long> slotIds;
    private String date;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getCourtId() { return courtId; }
    public void setCourtId(Long courtId) { this.courtId = courtId; }
    public List<Long> getSlotIds() { return slotIds; }
    public void setSlotIds(List<Long> slotIds) { this.slotIds = slotIds; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
