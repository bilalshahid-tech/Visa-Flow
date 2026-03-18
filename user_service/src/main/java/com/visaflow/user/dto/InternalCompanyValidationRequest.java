package com.visaflow.user.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InternalCompanyValidationRequest(
    @NotNull(message = "Company ID is required")
    UUID companyId,
    String requiredFeature
) {}
