package com.visaflow.cases.dto.response;

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
public class CaseStatusHistoryResponse {
    private UUID id;
    private UUID caseId;
    private CaseStatus oldStatus;
    private CaseStatus newStatus;
    private LocalDateTime changedAt;
    private String changedBy;
}
