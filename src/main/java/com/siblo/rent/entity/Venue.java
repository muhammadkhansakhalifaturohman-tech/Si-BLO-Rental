package com.siblo.rent.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String zone;

    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Court> courts = new ArrayList<>();

    public Venue() {}

    public Venue(Long id, String name, String address, String zone, Double latitude, Double longitude) {
        this.id = id; this.name = name; this.address = address; this.zone = zone;
        this.latitude = latitude; this.longitude = longitude;
    }

    public static VenueBuilder builder() { return new VenueBuilder(); }

    public static class VenueBuilder {
        private Long id; private String name; private String address; private String zone;
        private Double latitude; private Double longitude;
        VenueBuilder() {}
        public VenueBuilder id(Long id) { this.id = id; return this; }
        public VenueBuilder name(String name) { this.name = name; return this; }
        public VenueBuilder address(String address) { this.address = address; return this; }
        public VenueBuilder zone(String zone) { this.zone = zone; return this; }
        public VenueBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public VenueBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public Venue build() { return new Venue(id, name, address, zone, latitude, longitude); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public List<Court> getCourts() { return courts; }
    public void setCourts(List<Court> courts) { this.courts = courts; }
}
