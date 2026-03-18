package com.visaflow.user.dto;

public record ProfileUpdateRequest(
    String position,
    String department,
    String phoneNumber,
    String avatarUrl,
    String language,
    String timezone,
    Object metadata
) {}
