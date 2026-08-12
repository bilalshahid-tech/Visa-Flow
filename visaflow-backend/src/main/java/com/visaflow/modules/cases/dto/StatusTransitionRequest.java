package com.visaflow.modules.cases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.visaflow.modules.cases.entity.enums.CaseStatus;

@Data
public class StatusTransitionRequest {

    @NotNull(message = "New status is required")
    private CaseStatus newStatus;

    @NotBlank(message = "A note is required when changing case status")
    private String note;
}
