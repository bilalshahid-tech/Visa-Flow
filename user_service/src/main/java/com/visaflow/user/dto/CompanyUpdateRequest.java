package com.visaflow.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyUpdateRequest(
    @NotBlank(message = "Company name is required")
    String name,
    String phone,
    Object address,
    String website,
    String logo,
    String timezone,
    String language
) {}
