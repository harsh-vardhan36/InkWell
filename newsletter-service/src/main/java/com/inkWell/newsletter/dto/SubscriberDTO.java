package com.inkWell.newsletter.dto;

import com.inkWell.newsletter.domain.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberDTO {
    private Long id;
    private String email;
    private SubscriptionStatus status;
    private String preferences;
    private LocalDateTime subscribedAt;
}
