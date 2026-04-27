package com.inkWell.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest implements Serializable {
    private String to;
    private String subject;
    private String type; // e.g., "VERIFICATION", "PASSWORD_RESET", "LOGIN_ALERT"
    private Long userId;
    private Map<String, String> data;
}
