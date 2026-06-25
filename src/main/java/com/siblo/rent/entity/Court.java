package com.siblo.rent.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courts")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "surface_type")
    private String surfaceType;

    private Boolean indoor;

    @Column(name = "price_per_hour", nullable = false)
    private Integer pricePerHour;

    @Column(nullable = false)
    private Integer capacity;

    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtStatus status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "badge_label")
    private String badgeLabel;

    @Column(name = "payment_link_url")
    private String paymentLinkUrl;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeSlot> timeSlots = new ArrayList<>();

    public enum CourtStatus { ACTIVE, MAINTENANCE, INACTIVE }

    public Court() {}

    public Court(Long id, Venue venue, Sport sport, String name, String description, String surfaceType,
                 Boolean indoor, Integer pricePerHour, Integer capacity, Double rating, Integer reviewCount,
                 CourtStatus status, String imageUrl, String badgeLabel, String paymentLinkUrl,
                 LocalTime openTime, LocalTime closeTime) {
        this.id = id; this.venue = venue; this.sport = sport; this.name = name;
        this.description = description; this.surfaceType = surfaceType; this.indoor = indoor;
        this.pricePerHour = pricePerHour; this.capacity = capacity; this.rating = rating;
        this.reviewCount = reviewCount; this.status = status; this.imageUrl = imageUrl;
        this.badgeLabel = badgeLabel; this.paymentLinkUrl = paymentLinkUrl;
        this.openTime = openTime; this.closeTime = closeTime;
    }

    public static CourtBuilder builder() { return new CourtBuilder(); }

    public static class CourtBuilder {
        private Long id; private Venue venue; private Sport sport; private String name;
        private String description; private String surfaceType; private Boolean indoor;
        private Integer pricePerHour; private Integer capacity; private Double rating;
        private Integer reviewCount; private CourtStatus status; private String imageUrl;
        private String badgeLabel; private String paymentLinkUrl;
        private LocalTime openTime; private LocalTime closeTime;
        CourtBuilder() {}
        public CourtBuilder id(Long id) { this.id = id; return this; }
        public CourtBuilder venue(Venue venue) { this.venue = venue; return this; }
        public CourtBuilder sport(Sport sport) { this.sport = sport; return this; }
        public CourtBuilder name(String name) { this.name = name; return this; }
        public CourtBuilder description(String description) { this.description = description; return this; }
        public CourtBuilder surfaceType(String surfaceType) { this.surfaceType = surfaceType; return this; }
        public CourtBuilder indoor(Boolean indoor) { this.indoor = indoor; return this; }
        public CourtBuilder pricePerHour(Integer pricePerHour) { this.pricePerHour = pricePerHour; return this; }
        public CourtBuilder capacity(Integer capacity) { this.capacity = capacity; return this; }
        public CourtBuilder rating(Double rating) { this.rating = rating; return this; }
        public CourtBuilder reviewCount(Integer reviewCount) { this.reviewCount = reviewCount; return this; }
        public CourtBuilder status(CourtStatus status) { this.status = status; return this; }
        public CourtBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public CourtBuilder badgeLabel(String badgeLabel) { this.badgeLabel = badgeLabel; return this; }
        public CourtBuilder paymentLinkUrl(String paymentLinkUrl) { this.paymentLinkUrl = paymentLinkUrl; return this; }
        public CourtBuilder openTime(LocalTime openTime) { this.openTime = openTime; return this; }
        public CourtBuilder closeTime(LocalTime closeTime) { this.closeTime = closeTime; return this; }
        public Court build() { return new Court(id, venue, sport, name, description, surfaceType, indoor, pricePerHour, capacity, rating, reviewCount, status, imageUrl, badgeLabel, paymentLinkUrl, openTime, closeTime); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }
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
    public CourtStatus getStatus() { return status; }
    public void setStatus(CourtStatus status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getBadgeLabel() { return badgeLabel; }
    public void setBadgeLabel(String badgeLabel) { this.badgeLabel = badgeLabel; }
    public String getPaymentLinkUrl() { return paymentLinkUrl; }
    public void setPaymentLinkUrl(String paymentLinkUrl) { this.paymentLinkUrl = paymentLinkUrl; }
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
    public List<TimeSlot> getTimeSlots() { return timeSlots; }
    public void setTimeSlots(List<TimeSlot> timeSlots) { this.timeSlots = timeSlots; }
}
