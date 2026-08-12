package com.visaflow.modules.cases.service;

import com.visaflow.common.event.CaseEvent;
import com.visaflow.modules.auth.entity.User;
import com.visaflow.modules.auth.repository.UserRepository;
import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.cases.dto.*;
import com.visaflow.modules.cases.entity.*;
import com.visaflow.modules.cases.entity.enums.CaseStatus;
import com.visaflow.modules.cases.repository.*;
import com.visaflow.modules.document.entity.Document;
import com.visaflow.modules.document.entity.enums.DocumentStatus;
import com.visaflow.modules.document.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {

    private final VisaCaseRepository caseRepository;
    private final ClientRepository clientRepository;
    private final VisaTypeRepository visaTypeRepository;
    private final DocumentRequirementRepository requirementRepository;
    private final CaseStatusHistoryRepository statusHistoryRepository;
    private final CaseNoteRepository noteRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CaseStateMachine stateMachine;
    private final ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // Create case
    // -------------------------------------------------------------------------

    @Transactional
    public CaseResponse createCase(CreateCaseRequest request, UserPrincipal principal) {
        Client client = clientRepository.findByIdAndCompanyId(request.getClientId(), principal.getCompanyId())
                .orElseThrow(() -> new AccessDeniedException("Client not found or does not belong to your consultancy"));

        VisaType visaType = visaTypeRepository.findById(request.getVisaTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Visa type not found: " + request.getVisaTypeId()));

        String caseRef = generateCaseReference(principal.getCompanyId(), visaType.getCode());

        VisaCase visaCase = VisaCase.builder()
                .companyId(principal.getCompanyId())
                .client(client)
                .visaType(visaType)
                .caseReference(caseRef)
                .status(CaseStatus.DRAFT)
                .submissionDate(request.getSubmissionDate())
                .notes(request.getNotes())
                .createdBy(principal.getEmail())
                .updatedBy(principal.getEmail())
                .build();

        visaCase = caseRepository.save(visaCase);

        log.info("Case created: ref={} clientId={} visaType={} company={}",
                caseRef, client.getId(), visaType.getCode(), principal.getCompanyId());

        eventPublisher.publishEvent(new CaseEvent(this, visaCase.getId(), visaCase.getCompanyId(),
                principal.getUserId(), principal.getEmail(), "CASE_CREATED", null, CaseStatus.DRAFT.name()));

        return toCaseResponse(visaCase);
    }

    // -------------------------------------------------------------------------
    // Get case detail (full, with checklist, history, notes)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public CaseDetailResponse getCaseDetail(UUID caseId, UserPrincipal principal) {
        VisaCase visaCase = caseRepository.findDetailedByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        return buildDetailResponse(visaCase, principal);
    }

    // -------------------------------------------------------------------------
    // List cases (backlog)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<CaseResponse> listCases(UserPrincipal principal, CaseStatus status, Pageable pageable) {
        if (status != null) {
            return caseRepository.findByCompanyIdAndStatus(principal.getCompanyId(), status, pageable)
                    .map(this::toCaseResponse);
        }
        return caseRepository.findByCompanyId(principal.getCompanyId(), pageable)
                .map(this::toCaseResponse);
    }

    // -------------------------------------------------------------------------
    // Status transitions (validated by state machine)
    // -------------------------------------------------------------------------

    @Transactional
    public CaseDetailResponse transitionStatus(UUID caseId, StatusTransitionRequest request, UserPrincipal principal) {
        VisaCase visaCase = caseRepository.findDetailedByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        CaseStatus oldStatus = visaCase.getStatus();
        stateMachine.validateTransition(oldStatus, request.getNewStatus());

        visaCase.setStatus(request.getNewStatus());
        visaCase.setUpdatedBy(principal.getEmail());
        if (request.getNewStatus() == CaseStatus.APPROVED || request.getNewStatus() == CaseStatus.REJECTED) {
            visaCase.setDecisionDate(LocalDateTime.now());
        }

        CaseStatusHistory history = CaseStatusHistory.builder()
                .visaCase(visaCase)
                .oldStatus(oldStatus.name())
                .newStatus(request.getNewStatus().name())
                .changedById(principal.getUserId())
                .changedBy(principal.getEmail())
                .note(request.getNote())
                .build();

        statusHistoryRepository.save(history);
        caseRepository.save(visaCase);

        log.info("Case status transition: ref={} {} → {} by={} note='{}'",
                visaCase.getCaseReference(), oldStatus, request.getNewStatus(), principal.getEmail(), request.getNote());

        eventPublisher.publishEvent(new CaseEvent(this, visaCase.getId(), visaCase.getCompanyId(),
                principal.getUserId(), principal.getEmail(), "CASE_STATUS_CHANGED",
                oldStatus.name(), request.getNewStatus().name()));

        return buildDetailResponse(visaCase, principal);
    }

    // -------------------------------------------------------------------------
    // Allowed transitions
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<CaseStatus> getAllowedTransitions(UUID caseId, UserPrincipal principal) {
        VisaCase visaCase = caseRepository.findByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));
        return stateMachine.getAllowedTransitions(visaCase.getStatus());
    }

    // -------------------------------------------------------------------------
    // Notes
    // -------------------------------------------------------------------------

    @Transactional
    public CaseDetailResponse.NoteResponse addNote(UUID caseId, AddNoteRequest request, UserPrincipal principal) {
        VisaCase visaCase = caseRepository.findByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        CaseNote note = CaseNote.builder()
                .visaCase(visaCase)
                .authorId(principal.getUserId())
                .body(request.getBody())
                .build();

        note = noteRepository.save(note);
        log.info("Note added: caseId={} author={}", caseId, principal.getEmail());

        return CaseDetailResponse.NoteResponse.builder()
                .id(note.getId())
                .authorId(note.getAuthorId())
                .authorEmail(principal.getEmail())
                .body(note.getBody())
                .createdAt(note.getCreatedAt())
                .build();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private CaseDetailResponse buildDetailResponse(VisaCase visaCase, UserPrincipal principal) {
        // Build checklist: requirements + uploaded docs mapped by requirementId
        List<DocumentRequirement> requirements = visaCase.getVisaType() != null
                ? requirementRepository.findByVisaTypeIdOrderByDisplayOrderAsc(visaCase.getVisaType().getId())
                : List.of();

        List<Document> docs = documentRepository.findByCaseIdAndCompanyId(
                visaCase.getId(), principal.getCompanyId(), Pageable.unpaged()).getContent();

        Map<UUID, Document> docByReqId = docs.stream()
                .filter(d -> d.getRequirementId() != null)
                .collect(Collectors.toMap(Document::getRequirementId, d -> d, (a, b) -> b));

        List<CaseDetailResponse.ChecklistItemResponse> checklist = requirements.stream().map(req -> {
            Document upload = docByReqId.get(req.getId());
            return CaseDetailResponse.ChecklistItemResponse.builder()
                    .requirementId(req.getId())
                    .documentClass(req.getDocumentClass())
                    .label(req.getLabel())
                    .mandatory(req.isMandatory())
                    .displayOrder(req.getDisplayOrder())
                    .documentId(upload != null ? upload.getId() : null)
                    .documentStatus(upload != null ? upload.getStatus().name() : null)
                    .originalFilename(upload != null ? upload.getOriginalFilename() : null)
                    .reviewerNotes(upload != null ? upload.getReviewerNotes() : null)
                    .build();
        }).collect(Collectors.toList());

        long uploaded = checklist.stream().filter(c -> c.getDocumentId() != null).count();

        // Status history
        List<CaseStatusHistory> historyRows = visaCase.getStatusHistory();
        List<CaseDetailResponse.StatusHistoryResponse> historyResponse = historyRows.stream()
                .map(h -> CaseDetailResponse.StatusHistoryResponse.builder()
                        .fromStatus(friendlyStatus(h.getOldStatus()))
                        .toStatus(friendlyStatus(h.getNewStatus()))
                        .changedBy(h.getChangedBy())
                        .note(h.getNote())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());

        // Notes
        List<CaseNote> notes = noteRepository.findByVisaCaseIdOrderByCreatedAtAsc(visaCase.getId());
        Map<UUID, String> userEmailCache = new HashMap<>();
        List<CaseDetailResponse.NoteResponse> noteResponses = notes.stream().map(n -> {
            String email = userEmailCache.computeIfAbsent(n.getAuthorId(), uid ->
                    userRepository.findById(uid).map(User::getEmail).orElse("Unknown"));
            return CaseDetailResponse.NoteResponse.builder()
                    .id(n.getId()).authorId(n.getAuthorId()).authorEmail(email)
                    .body(n.getBody()).createdAt(n.getCreatedAt()).build();
        }).collect(Collectors.toList());

        Client client = visaCase.getClient();
        VisaType vt = visaCase.getVisaType();

        return CaseDetailResponse.builder()
                .id(visaCase.getId())
                .companyId(visaCase.getCompanyId())
                .caseReference(visaCase.getCaseReference())
                .status(friendlyStatus(visaCase.getStatus().name()))
                .allowedTransitions(stateMachine.getAllowedTransitions(visaCase.getStatus())
                        .stream().map(s -> friendlyStatus(s.name())).collect(Collectors.toList()))
                .clientId(client != null ? client.getId() : null)
                .clientName(client != null ? client.getFullName() : null)
                .clientPassportNumber(client != null ? client.getPassportNumber() : null)
                .clientNationality(client != null ? client.getNationality() : null)
                .clientDateOfBirth(client != null ? client.getDateOfBirth().toString() : null)
                .clientPhone(client != null ? client.getPhone() : null)
                .clientEmail(client != null ? client.getEmail() : null)
                .visaTypeId(vt != null ? vt.getId() : null)
                .visaTypeCode(vt != null ? vt.getCode() : null)
                .visaTypeName(vt != null ? vt.getName() : null)
                .checklist(checklist)
                .checklistTotal(checklist.size())
                .checklistUploaded((int) uploaded)
                .statusHistory(historyResponse)
                .notes(noteResponses)
                .assignedStaffId(visaCase.getAssignedStaffId())
                .submissionDate(visaCase.getSubmissionDate())
                .createdAt(visaCase.getCreatedAt())
                .updatedAt(visaCase.getUpdatedAt())
                .createdBy(visaCase.getCreatedBy())
                .build();
    }

    private String generateCaseReference(UUID companyId, String visaCode) {
        int year = LocalDateTime.now().getYear();
        // Use count-based sequence per company+visaCode+year
        long count = caseRepository.count() + 1;
        return String.format("VF-%d-%s-%05d", year, visaCode, count);
    }

    private CaseResponse toCaseResponse(VisaCase c) {
        Client client = c.getClient();
        VisaType vt = c.getVisaType();
        return CaseResponse.builder()
                .id(c.getId())
                .companyId(c.getCompanyId())
                .caseReference(c.getCaseReference())
                .status(c.getStatus().name())
                .visaTypeCode(vt != null ? vt.getCode() : null)
                .visaTypeName(vt != null ? vt.getName() : null)
                .clientId(client != null ? client.getId() : null)
                .clientName(client != null ? client.getFullName() : null)
                .assignedStaffId(c.getAssignedStaffId())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    /** Converts enum names to human-readable labels */
    private String friendlyStatus(String rawStatus) {
        return switch (rawStatus) {
            case "DRAFT"        -> "Draft";
            case "DOCS_PENDING" -> "Documents Pending";
            case "UNDER_REVIEW" -> "Under Review";
            case "SUBMITTED"    -> "Submitted";
            case "APPROVED"     -> "Approved";
            case "REJECTED"     -> "Rejected";
            case "CLOSED"       -> "Closed";
            default             -> rawStatus;
        };
    }
}
