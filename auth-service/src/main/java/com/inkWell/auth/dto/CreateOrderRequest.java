package com.inkWell.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private String plan;    // "PRO_TRIAL", "PRO_MONTHLY", "PRO_YEARLY"
    private String currency; // defaults to "INR"
}
