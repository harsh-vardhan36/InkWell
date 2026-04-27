package com.inkWell.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String plan;
    private String planExpiry;
    private String provider;
    private boolean isActive;
    private String createdAt;
    private String bio;
    private String avatarUrl;
    private String contactNumber;
}
