package com.visaflow.modules.cases.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDetailResponse {
    private UUID id;
    private UUID companyId;
    private String fullName;
    private String passportNumber;
    private String nationality;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ClientCaseSummaryResponse> cases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientCaseSummaryResponse {
        private UUID id;
        private String caseReference;
        private String visaProgramName;
        private String visaTypeName;
        private String status;
        private String currentStageName;
        private LocalDateTime createdAt;
    }
}
