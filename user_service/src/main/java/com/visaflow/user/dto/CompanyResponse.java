package com.visaflow.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyResponse(
    UUID id,
    String name,
    String taxId,
    String email,
    String phone,
    Object address,
    String website,
    String logoUrl,
    SubscriptionSummary subscription,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    CompanyStats stats
) {}
