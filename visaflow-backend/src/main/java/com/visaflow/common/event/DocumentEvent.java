package com.visaflow.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Fired after a Document is uploaded or its review status changes.
 * Replaces the document-events Kafka topic.
 */
@Getter
public class DocumentEvent extends ApplicationEvent {

    private final UUID documentId;
    private final UUID caseId;
    private final UUID companyId;
    private final UUID actorId;
    private final String actorEmail;
    private final String eventType;
    private final String documentType;
    private final String newStatus;

    public DocumentEvent(Object source, UUID documentId, UUID caseId, UUID companyId,
                         UUID actorId, String actorEmail, String eventType,
                         String documentType, String newStatus) {
        super(source);
        this.documentId = documentId;
        this.caseId = caseId;
        this.companyId = companyId;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.eventType = eventType;
        this.documentType = documentType;
        this.newStatus = newStatus;
    }
}
