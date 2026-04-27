package com.visaflow.cases.dto.response;

import com.visaflow.cases.domain.enums.CaseStage;
import com.visaflow.cases.domain.enums.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseResponse {
    private UUID id;
    private String caseReference;
    private CaseStatus status;
    private CaseStage currentStage;
    private LocalDateTime submissionDate;
    private LocalDateTime createdAt;
}
