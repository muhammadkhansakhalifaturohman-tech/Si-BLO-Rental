package com.siblo.rent.dto;

import com.siblo.rent.entity.User;

public class UserDTO {
    private Long id; private String name; private String email; private String role;
    private String membershipTier;

    public UserDTO() {}

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId()); dto.setName(user.getName()); dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setMembershipTier(user.getMembershipTier());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMembershipTier() { return membershipTier; }
    public void setMembershipTier(String membershipTier) { this.membershipTier = membershipTier; }
}
