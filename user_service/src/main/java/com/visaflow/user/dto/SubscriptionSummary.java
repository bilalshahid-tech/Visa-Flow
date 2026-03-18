package com.visaflow.user.dto;

public record SubscriptionSummary(
    String id,
    String planId,
    String planName,
    String status,
    java.time.LocalDateTime endDate
) {}
