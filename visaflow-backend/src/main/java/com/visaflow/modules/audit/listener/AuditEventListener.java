package com.visaflow.modules.audit.listener;

import com.visaflow.common.event.CaseEvent;
import com.visaflow.common.event.DocumentEvent;
import com.visaflow.modules.audit.entity.AuditLog;
import com.visaflow.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Consumes domain events and persists immutable audit records.
 *
 * Uses @TransactionalEventListener(phase = AFTER_COMMIT) to ensure:
 * - The audit write happens only after the originating transaction commits.
 * - If the main transaction rolls back, no phantom audit record is written.
 *
 * Durability trade-off: if the JVM crashes between COMMIT and listener execution,
 * this audit entry will be lost. This is acceptable for a pre-launch system.
 * For guaranteed delivery, promote to a DB outbox pattern.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCaseEvent(CaseEvent event) {
        try {
            AuditLog log = AuditLog.builder()
                    .companyId(event.getCompanyId())
                    .entityType("CASE")
                    .entityId(event.getCaseId())
                    .eventType(event.getEventType())
                    .actorId(event.getActorId())
                    .actorType("USER")
                    .payload(Map.of(
                            "oldStatus", String.valueOf(event.getOldStatus()),
                            "newStatus", String.valueOf(event.getNewStatus())
                    ))
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to write audit log for CaseEvent {}: {}", event.getCaseId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentEvent(DocumentEvent event) {
        try {
            AuditLog log = AuditLog.builder()
                    .companyId(event.getCompanyId())
                    .entityType("DOCUMENT")
                    .entityId(event.getDocumentId())
                    .eventType(event.getEventType())
                    .actorId(event.getActorId())
                    .actorType("USER")
                    .payload(Map.of(
                            "caseId", event.getCaseId().toString(),
                            "documentType", event.getDocumentType(),
                            "newStatus", String.valueOf(event.getNewStatus())
                    ))
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to write audit log for DocumentEvent {}: {}", event.getDocumentId(), e.getMessage());
        }
    }
}
