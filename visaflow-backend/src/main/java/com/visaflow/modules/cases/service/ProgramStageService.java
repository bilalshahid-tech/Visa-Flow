package com.visaflow.modules.cases.service;

import com.visaflow.common.event.CaseEvent;
import com.visaflow.modules.cases.dto.CaseDetailResponse;
import com.visaflow.modules.cases.entity.*;
import com.visaflow.modules.cases.entity.enums.CaseStatus;
import com.visaflow.modules.cases.repository.*;
import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.document.entity.Document;
import com.visaflow.modules.document.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles stage-aware checklist evaluation and stage advancement logic.
 * Kept separate from CaseService to isolate conditional rule evaluation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramStageService {

    private final VisaCaseRepository caseRepository;
    private final VisaProgramStageRepository stageRepository;
    private final StageDocumentRequirementRepository requirementRepository;
    private final CaseStageHistoryRepository stageHistoryRepository;
    private final CaseStatusHistoryRepository statusHistoryRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // Build stage stepper (all stages for the case's program)
    // -------------------------------------------------------------------------

    public List<CaseDetailResponse.StageResponse> buildStageStepper(VisaCase visaCase) {
        if (visaCase.getVisaProgram() == null) return List.of();

        List<VisaProgramStage> stages = stageRepository
                .findByVisaProgramIdOrderBySequenceOrderAsc(visaCase.getVisaProgram().getId());

        UUID currentStageId = visaCase.getCurrentStage() != null ? visaCase.getCurrentStage().getId() : null;

        // Build a map of stage histories for this case
        List<CaseStageHistory> histories = stageHistoryRepository
                .findByCaseVisaCaseIdOrderByEnteredAtAsc(visaCase.getId());
        Map<UUID, CaseStageHistory> historyByStageId = histories.stream()
                .collect(Collectors.toMap(h -> h.getStage().getId(), h -> h, (a, b) -> b));

        return stages.stream().map(stage -> {
            CaseStageHistory hist = historyByStageId.get(stage.getId());
            boolean isCurrent = stage.getId().equals(currentStageId);
            boolean isCompleted = hist != null && hist.getCompletedAt() != null;
            return CaseDetailResponse.StageResponse.builder()
                    .id(stage.getId())
                    .sequenceOrder(stage.getSequenceOrder())
                    .name(stage.getName())
                    .description(stage.getDescription())
                    .isCurrent(isCurrent)
                    .isCompleted(isCompleted)
                    .enteredAt(hist != null ? hist.getEnteredAt() : null)
                    .completedAt(hist != null ? hist.getCompletedAt() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Build evaluated checklist for a given stage
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<CaseDetailResponse.StageChecklistItemResponse> buildStageChecklist(
            UUID caseId, UUID stageId, UserPrincipal principal) {

        VisaCase visaCase = caseRepository.findByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new AccessDeniedException("Case not found or access denied"));

        return buildStageChecklistInternal(visaCase, stageId);
    }

    public List<CaseDetailResponse.StageChecklistItemResponse> buildStageChecklistInternal(
            VisaCase visaCase, UUID stageId) {

        List<StageDocumentRequirement> requirements =
                requirementRepository.findByStageIdOrderByDisplayOrderAsc(stageId);

        // Fetch all docs for this case, indexed by stageDocumentRequirementId
        List<Document> docs = documentRepository.findByCaseIdAndCompanyId(
                visaCase.getId(), visaCase.getCompanyId(), Pageable.unpaged()).getContent();

        Map<UUID, Document> docByStageReqId = docs.stream()
                .filter(d -> d.getStageDocumentRequirementId() != null)
                .collect(Collectors.toMap(Document::getStageDocumentRequirementId, d -> d, (a, b) -> b));

        return requirements.stream()
                .filter(req -> shouldShowRequirement(req, visaCase))
                .map(req -> {
                    boolean conditionallyMandatory = evaluateConditional(req, visaCase);
                    Document upload = docByStageReqId.get(req.getId());
                    return CaseDetailResponse.StageChecklistItemResponse.builder()
                            .requirementId(req.getId())
                            .documentType(req.getDocumentType())
                            .displayName(req.getDisplayName())
                            .mandatory(req.isMandatory())
                            .conditionallyMandatory(conditionallyMandatory)
                            .notes(req.getNotes())
                            .displayOrder(req.getDisplayOrder())
                            .documentId(upload != null ? upload.getId() : null)
                            .documentStatus(upload != null ? upload.getStatus().name() : null)
                            .originalFilename(upload != null ? upload.getOriginalFilename() : null)
                            .reviewerNotes(upload != null ? upload.getReviewerNotes() : null)
                            .build();
                }).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Advance stage (validate all mandatory docs uploaded, then move forward)
    // -------------------------------------------------------------------------

    @Transactional
    public void advanceStage(UUID caseId, UserPrincipal principal) {
        VisaCase visaCase = caseRepository.findDetailedByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        VisaProgramStage currentStage = visaCase.getCurrentStage();
        if (currentStage == null) {
            throw new IllegalStateException("Case has no current stage assigned.");
        }

        // Validate all mandatory (incl conditionally-mandatory) requirements are uploaded
        List<CaseDetailResponse.StageChecklistItemResponse> checklist =
                buildStageChecklistInternal(visaCase, currentStage.getId());

        List<String> missing = checklist.stream()
                .filter(item -> (item.isMandatory() || item.isConditionallyMandatory()) && item.getDocumentId() == null)
                .map(CaseDetailResponse.StageChecklistItemResponse::getDisplayName)
                .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Cannot advance stage. Missing mandatory documents: " + String.join(", ", missing));
        }

        // Mark current stage history as complete
        stageHistoryRepository.findByCaseVisaCaseIdAndStageIdAndCompletedAtIsNull(caseId, currentStage.getId())
                .ifPresent(hist -> {
                    hist.setCompletedAt(LocalDateTime.now());
                    hist.setCompletedBy(principal.getUserId());
                    stageHistoryRepository.save(hist);
                });

        // Find next stage
        Optional<VisaProgramStage> nextStageOpt = stageRepository.findByProgramIdAndOrder(
                currentStage.getVisaProgram().getId(), currentStage.getSequenceOrder() + 1);

        if (nextStageOpt.isPresent()) {
            VisaProgramStage nextStage = nextStageOpt.get();
            visaCase.setCurrentStage(nextStage);
            if (visaCase.getStatus() == CaseStatus.DRAFT) {
                visaCase.setStatus(CaseStatus.DOCS_PENDING);
            }
            caseRepository.save(visaCase);

            // Log new stage history entry
            CaseStageHistory newHistory = CaseStageHistory.builder()
                    .visaCase(visaCase)
                    .stage(nextStage)
                    .build();
            stageHistoryRepository.save(newHistory);

            log.info("Case {} advanced to stage '{}' (order {})", caseId, nextStage.getName(), nextStage.getSequenceOrder());
        } else {
            // No next stage — all stages complete; transition case status to SUBMITTED
            visaCase.setCurrentStage(null);
            CaseStatus oldStatus = visaCase.getStatus();
            visaCase.setStatus(CaseStatus.SUBMITTED);
            visaCase.setUpdatedBy(principal.getEmail());
            caseRepository.save(visaCase);

            CaseStatusHistory history = CaseStatusHistory.builder()
                    .visaCase(visaCase)
                    .oldStatus(oldStatus.name())
                    .newStatus(CaseStatus.SUBMITTED.name())
                    .changedById(principal.getUserId())
                    .changedBy(principal.getEmail())
                    .note("All case stages completed automatically.")
                    .build();
            statusHistoryRepository.save(history);

            eventPublisher.publishEvent(new CaseEvent(this, visaCase.getId(), visaCase.getCompanyId(),
                    principal.getUserId(), principal.getEmail(), "CASE_STATUS_CHANGED",
                    oldStatus.name(), CaseStatus.SUBMITTED.name()));

            log.info("Case {} completed all stages. Status transitioned to SUBMITTED.", caseId);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * A requirement should be shown if:
     * - it has no conditional field (unconditional), OR
     * - it has a conditional field that matches the case attribute (conditional → show as mandatory)
     */
    private boolean shouldShowRequirement(StageDocumentRequirement req, VisaCase visaCase) {
        if (req.getConditionalField() == null) return true;
        // Show conditional requirements only when they evaluate to true (i.e. they become mandatory)
        return evaluateConditional(req, visaCase);
    }

    /**
     * Evaluates the conditional field/operator/value against the case.
     * Returns true if the condition applies (making the requirement additionally mandatory).
     * Currently supports only EQUALS operator.
     */
    private boolean evaluateConditional(StageDocumentRequirement req, VisaCase visaCase) {
        if (req.getConditionalField() == null || req.getConditionalOperator() == null) return false;

        String caseValue = resolveCaseAttribute(req.getConditionalField(), visaCase);
        if (caseValue == null) return false;

        return switch (req.getConditionalOperator().toUpperCase()) {
            case "EQUALS" -> caseValue.equalsIgnoreCase(req.getConditionalValue());
            default -> {
                log.warn("Unknown conditional operator '{}' on requirement {}", req.getConditionalOperator(), req.getId());
                yield false;
            }
        };
    }

    /** Resolves a named case attribute to its string value for conditional evaluation. */
    private String resolveCaseAttribute(String fieldName, VisaCase visaCase) {
        return switch (fieldName) {
            case "job_role_category" -> visaCase.getJobRoleCategory();
            default -> {
                log.warn("Unknown conditional field '{}' — cannot evaluate condition", fieldName);
                yield null;
            }
        };
    }
}
