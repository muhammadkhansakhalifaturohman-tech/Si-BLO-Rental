package com.siblo.rent.dto;

import java.time.LocalTime;

public class CourtRequest {
    private String name; private String description; private String surfaceType;
    private Boolean indoor; private Integer pricePerHour; private Integer capacity;
    private String status; private Long sportId; private Long venueId; private String imageUrl;
    private String paymentLinkUrl;
    private LocalTime openTime; private LocalTime closeTime;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSurfaceType() { return surfaceType; }
    public void setSurfaceType(String surfaceType) { this.surfaceType = surfaceType; }
    public Boolean getIndoor() { return indoor; }
    public void setIndoor(Boolean indoor) { this.indoor = indoor; }
    public Integer getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Integer pricePerHour) { this.pricePerHour = pricePerHour; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getSportId() { return sportId; }
    public void setSportId(Long sportId) { this.sportId = sportId; }
    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPaymentLinkUrl() { return paymentLinkUrl; }
    public void setPaymentLinkUrl(String paymentLinkUrl) { this.paymentLinkUrl = paymentLinkUrl; }
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}
