package com.visaflow.user.dto;

import java.math.BigDecimal;

public record SubscriptionPlanDto(
    String id,
    String name,
    BigDecimal price,
    String currency,
    String interval,
    SubscriptionFeatures features
) {}
