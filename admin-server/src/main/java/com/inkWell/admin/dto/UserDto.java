package com.inkWell.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @JsonProperty("userId")
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String plan;
    @JsonProperty("isActive")
    private boolean active;
    private String provider;
    private LocalDateTime createdAt;
}
