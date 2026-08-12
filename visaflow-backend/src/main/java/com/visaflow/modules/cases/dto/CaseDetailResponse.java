package com.visaflow.modules.cases.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CaseDetailResponse {
    private UUID id;
    private UUID companyId;
    private String caseReference;
    private String status;
    private List<String> allowedTransitions;

    // Client summary
    private UUID clientId;
    private String clientName;
    private String clientPassportNumber;
    private String clientNationality;
    private String clientDateOfBirth;
    private String clientPhone;
    private String clientEmail;

    // Visa type summary
    private UUID visaTypeId;
    private String visaTypeCode;
    private String visaTypeName;

    // Checklist: requirements with uploaded doc status
    private List<ChecklistItemResponse> checklist;
    private int checklistTotal;
    private int checklistUploaded;

    // Status history
    private List<StatusHistoryResponse> statusHistory;

    // Notes
    private List<NoteResponse> notes;

    private UUID assignedStaffId;
    private LocalDateTime submissionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    @Data
    @Builder
    public static class ChecklistItemResponse {
        private UUID requirementId;
        private String documentClass;
        private String label;
        private boolean mandatory;
        private int displayOrder;
        // Uploaded document (null if not yet uploaded)
        private UUID documentId;
        private String documentStatus; // PENDING, APPROVED, REJECTED, or null
        private String originalFilename;
        private String reviewerNotes;
    }

    @Data
    @Builder
    public static class StatusHistoryResponse {
        private String fromStatus;
        private String toStatus;
        private String changedBy;
        private String note;
        private LocalDateTime changedAt;
    }

    @Data
    @Builder
    public static class NoteResponse {
        private UUID id;
        private UUID authorId;
        private String authorEmail;
        private String body;
        private LocalDateTime createdAt;
    }
}
