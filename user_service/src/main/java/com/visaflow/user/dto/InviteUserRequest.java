package com.visaflow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    @NotBlank(message = "Role is required")
    String role,
    String position,
    String message
) {}
