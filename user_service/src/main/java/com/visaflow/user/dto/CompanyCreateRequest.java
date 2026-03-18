package com.visaflow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyCreateRequest(
    @NotBlank(message = "Company name is required")
    String name,
    String taxId,
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    @NotBlank(message = "Phone number is required")
    String phone,
    Object address,
    String website,
    String logo,
    String timezone,
    String language
) {}
