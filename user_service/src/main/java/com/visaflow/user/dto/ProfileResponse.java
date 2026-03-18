package com.visaflow.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    UUID userId,
    UUID companyId,
    String firstName,
    String lastName,
    String email,
    String role,
    String position,
    String department,
    String phoneNumber,
    String avatarUrl,
    String language,
    String timezone,
    Boolean emailVerified,
    LocalDateTime lastActive,
    LocalDateTime createdAt,
    Object metadata
) {}
