package com.visaflow.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserValidationRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;
    private List<String> requiredRoles;
    private List<String> requiredPermissions;
}
