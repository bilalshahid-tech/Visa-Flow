package com.visaflow.cases.event;

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
public class CaseEventPayload {
    private UUID caseId;
    private String caseReference;
    private String eventType;
    private CaseStatus status;
    private LocalDateTime timestamp;
}
