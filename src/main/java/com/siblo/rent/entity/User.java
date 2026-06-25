package com.siblo.rent.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "membership_tier")
    private String membershipTier;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    public enum Role {
        MEMBER, ADMIN
    }

    public User() {}

    public User(Long id, String name, String email, String password, Role role, String membershipTier, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.membershipTier = membershipTier;
        this.avatarUrl = avatarUrl;
    }

    public static UserBuilder builder() { return new UserBuilder(); }

    public static class UserBuilder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private Role role;
        private String membershipTier;
        private String avatarUrl;

        UserBuilder() {}

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder membershipTier(String membershipTier) { this.membershipTier = membershipTier; return this; }
        public UserBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public User build() { return new User(id, name, email, password, role, membershipTier, avatarUrl); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getMembershipTier() { return membershipTier; }
    public void setMembershipTier(String membershipTier) { this.membershipTier = membershipTier; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}
