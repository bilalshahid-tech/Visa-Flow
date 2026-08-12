package com.visaflow.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Fired after a VisaCase is created or its status changes.
 * Consumed in-process by AuditEventListener and NotificationEventListener.
 * Using @TransactionalEventListener(phase = AFTER_COMMIT) ensures listeners
 * only run once the originating DB transaction has committed successfully.
 */
@Getter
public class CaseEvent extends ApplicationEvent {

    private final UUID caseId;
    private final UUID companyId;
    private final UUID actorId;
    private final String actorEmail;
    private final String eventType;
    private final String oldStatus;
    private final String newStatus;

    public CaseEvent(Object source, UUID caseId, UUID companyId, UUID actorId,
                     String actorEmail, String eventType, String oldStatus, String newStatus) {
        super(source);
        this.caseId = caseId;
        this.companyId = companyId;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.eventType = eventType;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
