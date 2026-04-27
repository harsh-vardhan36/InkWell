package com.inkWell.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {
    private String fullName;
    private String bio;
    private String avatarUrl;

    public String getFullName() { return fullName; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
}
