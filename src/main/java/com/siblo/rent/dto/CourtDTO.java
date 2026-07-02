package com.siblo.rent.dto;

import com.siblo.rent.entity.Court;
import java.time.LocalTime;

public class CourtDTO {
    private Long id; private String name; private String description; private String surfaceType;
    private Boolean indoor; private Integer pricePerHour; private Integer capacity;
    private Double rating; private Integer reviewCount; private String status;
    private String imageUrl; private String badgeLabel; private String paymentLinkUrl;
    private Long sportId; private String sportName; private String venueName; private String venueZone;
    private LocalTime openTime; private LocalTime closeTime;

    public CourtDTO() {}

    public static CourtDTO fromEntity(Court court) {
        CourtDTO dto = new CourtDTO();
        dto.setId(court.getId()); dto.setName(court.getName());
        dto.setDescription(court.getDescription()); dto.setSurfaceType(court.getSurfaceType());
        dto.setIndoor(court.getIndoor()); dto.setPricePerHour(court.getPricePerHour());
        dto.setCapacity(court.getCapacity()); dto.setRating(court.getRating());
        dto.setReviewCount(court.getReviewCount());
        dto.setStatus(court.getStatus() != null ? court.getStatus().name() : null);
        dto.setImageUrl(court.getImageUrl()); dto.setBadgeLabel(court.getBadgeLabel());
        dto.setPaymentLinkUrl(court.getPaymentLinkUrl());
        dto.setSportId(court.getSport() != null ? court.getSport().getId() : null);
        dto.setSportName(court.getSport() != null ? court.getSport().getName() : null);
        dto.setVenueName(court.getVenue() != null ? court.getVenue().getName() : null);
        dto.setVenueZone(court.getVenue() != null ? court.getVenue().getZone() : null);
        dto.setOpenTime(court.getOpenTime());
        dto.setCloseTime(court.getCloseTime());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getBadgeLabel() { return badgeLabel; }
    public void setBadgeLabel(String badgeLabel) { this.badgeLabel = badgeLabel; }
    public String getPaymentLinkUrl() { return paymentLinkUrl; }
    public void setPaymentLinkUrl(String paymentLinkUrl) { this.paymentLinkUrl = paymentLinkUrl; }
    public Long getSportId() { return sportId; }
    public void setSportId(Long sportId) { this.sportId = sportId; }

    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getVenueZone() { return venueZone; }
    public void setVenueZone(String venueZone) { this.venueZone = venueZone; }
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}
