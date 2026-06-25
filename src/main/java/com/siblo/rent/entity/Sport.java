package com.siblo.rent.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sports")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String icon;

    @Column(name = "location_count")
    private Integer locationCount;

    @Column(name = "image_url")
    private String imageUrl;

    public Sport() {}

    public Sport(Long id, String name, String slug, String icon, Integer locationCount, String imageUrl) {
        this.id = id; this.name = name; this.slug = slug; this.icon = icon;
        this.locationCount = locationCount; this.imageUrl = imageUrl;
    }

    public static SportBuilder builder() { return new SportBuilder(); }

    public static class SportBuilder {
        private Long id; private String name; private String slug; private String icon;
        private Integer locationCount; private String imageUrl;
        SportBuilder() {}
        public SportBuilder id(Long id) { this.id = id; return this; }
        public SportBuilder name(String name) { this.name = name; return this; }
        public SportBuilder slug(String slug) { this.slug = slug; return this; }
        public SportBuilder icon(String icon) { this.icon = icon; return this; }
        public SportBuilder locationCount(Integer locationCount) { this.locationCount = locationCount; return this; }
        public SportBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Sport build() { return new Sport(id, name, slug, icon, locationCount, imageUrl); }
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
