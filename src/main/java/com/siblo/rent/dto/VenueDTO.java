package com.siblo.rent.dto;

import com.siblo.rent.entity.Venue;
import java.util.List;
import java.util.stream.Collectors;

public class VenueDTO {
    private Long id; private String name; private String address; private String zone;
    private Double latitude; private Double longitude;
    private List<CourtDTO> courts;

    public VenueDTO() {}

    public static VenueDTO fromEntity(Venue venue) {
        VenueDTO dto = new VenueDTO();
        dto.setId(venue.getId()); dto.setName(venue.getName());
        dto.setAddress(venue.getAddress()); dto.setZone(venue.getZone());
        dto.setLatitude(venue.getLatitude()); dto.setLongitude(venue.getLongitude());
        if (venue.getCourts() != null && !venue.getCourts().isEmpty()) {
            dto.setCourts(venue.getCourts().stream().map(CourtDTO::fromEntity).collect(Collectors.toList()));
        }
        return dto;
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
    public List<CourtDTO> getCourts() { return courts; }
    public void setCourts(List<CourtDTO> courts) { this.courts = courts; }
}
