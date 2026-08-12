package com.visaflow.modules.cases.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CaseResponse {
    private UUID id;
    private UUID companyId;
    private String caseReference;
    private String status;
    private String visaTypeCode;
    private String visaTypeName;
    private UUID clientId;
    private String clientName;
    private UUID assignedStaffId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
