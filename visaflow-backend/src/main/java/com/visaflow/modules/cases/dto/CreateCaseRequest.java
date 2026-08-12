package com.visaflow.modules.cases.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateCaseRequest {

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotNull(message = "Visa type ID is required")
    private UUID visaTypeId;

    private LocalDateTime submissionDate;

    private String notes;
}
