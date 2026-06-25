package com.siblo.rent.dto;

import com.siblo.rent.entity.Sport;

public class SportDTO {
    private Long id; private String name; private String slug; private String icon;
    private Integer locationCount; private String imageUrl;

    public SportDTO() {}

    public static SportDTO fromEntity(Sport sport) {
        SportDTO dto = new SportDTO();
        dto.setId(sport.getId()); dto.setName(sport.getName());
        dto.setSlug(sport.getSlug()); dto.setIcon(sport.getIcon());
        dto.setLocationCount(sport.getLocationCount()); dto.setImageUrl(sport.getImageUrl());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getLocationCount() { return locationCount; }
    public void setLocationCount(Integer locationCount) { this.locationCount = locationCount; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
