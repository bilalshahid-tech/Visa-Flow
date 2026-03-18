package com.visaflow.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SubscriptionCreateRequest(
    @NotBlank(message = "Plan ID is required")
    String planId,
    @NotBlank(message = "Interval is required")
    String interval,
    String paymentMethodId,
    String promoCode
) {}
