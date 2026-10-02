package com.visaflow.modules.cases.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    // Visa program (new) — kept alongside old visaType for backward compat
    private UUID visaProgramId;
    private String visaProgramName;
    private String jobRoleCategory;

    // Legacy visa type — still populated for old cases that pre-date the migration
    private UUID visaTypeId;
    private String visaTypeCode;
    private String visaTypeName;

    // Stage stepper (new) — null/empty for old cases
    private List<StageResponse> stages;
    private String currentStageId;
    private String currentStageName;

    // Current stage checklist with conditional evaluation applied (new)
    private List<StageChecklistItemResponse> currentStageChecklist;

    // Legacy flat checklist — kept for old cases (visa_type-based flow)
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

    // -------------------------------------------------------------------------
    // Nested response types
    // -------------------------------------------------------------------------

@Data
@Builder
public static class StageResponse {
    private UUID id;
    private int sequenceOrder;
    private String name;
    private String description;

    @JsonProperty("isCurrent")
    private boolean isCurrent;

    @JsonProperty("isCompleted")
    private boolean isCompleted;

    private LocalDateTime enteredAt;
    private LocalDateTime completedAt;
}

    /** Stage-aware checklist item — tied to a StageDocumentRequirement */
    @Data
    @Builder
    public static class StageChecklistItemResponse {
        private UUID requirementId;         // StageDocumentRequirement.id
        private String documentType;
        private String displayName;
        private boolean mandatory;           // base is_mandatory flag
        private boolean conditionallyMandatory; // true when conditional field matches
        private String notes;
        private int displayOrder;
        // Uploaded document (null if not yet uploaded for this stage requirement)
        private UUID documentId;
        private String documentStatus;       // PENDING_REVIEW, APPROVED, REJECTED, or null
        private String originalFilename;
        private String reviewerNotes;
    }

    /** Legacy checklist item (kept for old visa_type-based cases) */
    @Data
    @Builder
    public static class ChecklistItemResponse {
        private UUID requirementId;
        private String documentClass;
        private String label;
        private boolean mandatory;
        private int displayOrder;
        private UUID documentId;
        private String documentStatus;
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
