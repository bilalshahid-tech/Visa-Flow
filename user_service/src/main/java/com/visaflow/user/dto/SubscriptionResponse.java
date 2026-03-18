package com.visaflow.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public record SubscriptionResponse(
    UUID id,
    UUID companyId,
    SubscriptionPlanDto plan,
    String status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Boolean autoRenew,
    LocalDateTime trialEndsAt,
    LocalDateTime cancelledAt,
    Object paymentMethod,
    List<Object> invoices
) {}
