package com.visaflow.modules.cases.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateCaseRequest {

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    /**
     * New: required for stage-aware case creation.
     * Replaces the old visaTypeId for new cases.
     */
    @NotNull(message = "Visa Program ID is required")
    private UUID visaProgramId;

    /**
     * Optional: job role category that drives conditional document requirements.
     * Example values: TOURISM, SERVICES, CONSTRUCTION, HEALTHCARE, TECHNOLOGY, OTHER
     */
    private String jobRoleCategory;

    private LocalDateTime submissionDate;

    private String notes;
}
