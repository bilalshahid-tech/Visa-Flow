package com.visaflow.cases.dto.request;

import com.visaflow.cases.domain.enums.CaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCaseStatusRequest {
    @NotNull(message = "New Status cannot be null")
    private CaseStatus newStatus;

    private String reason;
}
