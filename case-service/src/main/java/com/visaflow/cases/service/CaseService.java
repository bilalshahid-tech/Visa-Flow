package com.visaflow.cases.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visaflow.cases.domain.entity.Case;
import com.visaflow.cases.domain.entity.CaseStatusHistory;
import com.visaflow.cases.domain.entity.CaseTimeline;
import com.visaflow.cases.domain.enums.CaseStage;
import com.visaflow.cases.domain.enums.CaseStatus;
import com.visaflow.cases.dto.request.CreateCaseRequest;
import com.visaflow.cases.dto.request.UpdateCaseStatusRequest;
import com.visaflow.cases.dto.response.CaseResponse;
import com.visaflow.cases.dto.response.CaseStatusHistoryResponse;
import com.visaflow.cases.event.CaseEventPayload;
import com.visaflow.cases.exception.EntityNotFoundException;
import com.visaflow.cases.exception.UnauthorizedException;
import com.visaflow.cases.repository.CaseRepository;
import com.visaflow.cases.repository.CaseStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseStatusHistoryRepository historyRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String TOPIC = "case-events";

    @Transactional
    public CaseResponse createCase(CreateCaseRequest request, String userId, UUID companyId) {
        String caseRef = "CASE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Case newCase = Case.builder()
                .applicantId(request.getApplicantId())
                .companyId(companyId)
                .caseReference(caseRef)
                .visaType(request.getVisaType())
                .status(CaseStatus.INITIAL_REVIEW)
                .currentStage(CaseStage.INITIAL_REVIEW)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        CaseTimeline timeline = CaseTimeline.builder()
                .visaCase(newCase)
                .build();
        newCase.setTimeline(timeline);

        Case savedCase = caseRepository.save(newCase);
        
        publishCaseEvent(savedCase, "CASE_CREATED");

        return mapToResponse(savedCase);
    }

    @Transactional
    public CaseResponse updateCaseStatus(UUID caseId, UpdateCaseStatusRequest request, String userId, UUID companyId) {
        Case existingCase = getCaseEntityByIdAndCompany(caseId, companyId);

        CaseStatus oldStatus = existingCase.getStatus();
        CaseStatus newStatus = request.getNewStatus();

        if (oldStatus == newStatus) {
            throw new IllegalArgumentException("Status is already " + newStatus);
        }

        existingCase.setStatus(newStatus);
        
        // Basic mapping logic for stage progression based on status
        if (newStatus == CaseStatus.APPROVED || newStatus == CaseStatus.REJECTED || newStatus == CaseStatus.WITHDRAWN) {
            existingCase.setDecisionDate(LocalDateTime.now());
            existingCase.setCurrentStage(CaseStage.DECISION);
        }

        existingCase.setUpdatedBy(userId);

        CaseStatusHistory history = CaseStatusHistory.builder()
                .visaCase(existingCase)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(request.getReason())
                .changedBy(userId)
                .build();

        historyRepository.save(history);
        Case savedCase = caseRepository.save(existingCase);

        publishCaseEvent(savedCase, "CASE_STATUS_CHANGED");

        return mapToResponse(savedCase);
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getCasesByUser(UUID companyId) {
        return caseRepository.findByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CaseResponse getCaseById(UUID caseId, UUID companyId) {
        return mapToResponse(getCaseEntityByIdAndCompany(caseId, companyId));
    }

    @Transactional(readOnly = true)
    public List<CaseStatusHistoryResponse> getCaseHistory(UUID caseId, UUID companyId) {
        // Validate tenancy first
        getCaseEntityByIdAndCompany(caseId, companyId);
        
        return historyRepository.findByCaseIdOrderByChangedAtDesc(caseId).stream()
                .map(history -> CaseStatusHistoryResponse.builder()
                        .id(history.getId())
                        .caseId(history.getVisaCase().getId())
                        .oldStatus(history.getOldStatus())
                        .newStatus(history.getNewStatus())
                        .changedAt(history.getChangedAt())
                        .changedBy(history.getChangedBy())
                        .build())
                .collect(Collectors.toList());
    }

    private Case getCaseEntityByIdAndCompany(UUID caseId, UUID companyId) {
        Case existingCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found with ID: " + caseId));

        if (!existingCase.getCompanyId().equals(companyId)) {
            throw new UnauthorizedException("You are not authorized to access this case.");
        }

        return existingCase;
    }

    private void publishCaseEvent(Case caseEntity, String eventType) {
        try {
            CaseEventPayload payload = CaseEventPayload.builder()
                    .caseId(caseEntity.getId())
                    .caseReference(caseEntity.getCaseReference())
                    .eventType(eventType)
                    .status(caseEntity.getStatus())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(TOPIC, caseEntity.getId().toString(), payload);
            log.info("Published {} event for case: {}", eventType, caseEntity.getId());
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for case: " + caseEntity.getId(), e);
        }
    }

    private CaseResponse mapToResponse(Case caseEntity) {
        return CaseResponse.builder()
                .id(caseEntity.getId())
                .caseReference(caseEntity.getCaseReference())
                .status(caseEntity.getStatus())
                .currentStage(caseEntity.getCurrentStage())
                .submissionDate(caseEntity.getSubmissionDate())
                .createdAt(caseEntity.getCreatedAt())
                .build();
    }
}
