package com.visaflow.modules.notification.listener;

import com.visaflow.common.event.CaseEvent;
import com.visaflow.common.event.DocumentEvent;
import com.visaflow.modules.notification.entity.Notification;
import com.visaflow.modules.notification.entity.enums.NotificationStatus;
import com.visaflow.modules.notification.entity.enums.NotificationType;
import com.visaflow.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Consumes domain events and dispatches email notifications.
 *
 * @TransactionalEventListener(AFTER_COMMIT) ensures emails are only sent
 * after the originating database transaction successfully commits.
 * Failed email sends are recorded in the notifications table for retry tracking.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCaseStatusChanged(CaseEvent event) {
        if (event.getActorEmail() == null) return;

        String subject = "VisaFlow: Case Status Updated";
        String body = String.format(
                "Your visa case status has been updated from %s to %s.",
                event.getOldStatus(), event.getNewStatus()
        );

        Notification notification = Notification.builder()
                .companyId(event.getCompanyId())
                .caseId(event.getCaseId())
                .recipientEmail(event.getActorEmail())
                .notificationType(NotificationType.CASE_STATUS_CHANGED)
                .subject(subject)
                .body(body)
                .status(NotificationStatus.PENDING)
                .build();
        notification = notificationRepository.save(notification);

        try {
            sendEmail(event.getActorEmail(), subject, body);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send case status notification to {}: {}", event.getActorEmail(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
        }
        notificationRepository.save(notification);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentUploaded(DocumentEvent event) {
        if (event.getActorEmail() == null) return;

        String subject = "VisaFlow: Document Upload Confirmed";
        String body = String.format(
                "Your document of type %s has been uploaded and is pending review.",
                event.getDocumentType()
        );

        Notification notification = Notification.builder()
                .companyId(event.getCompanyId())
                .caseId(event.getCaseId())
                .recipientEmail(event.getActorEmail())
                .notificationType(NotificationType.DOCUMENT_UPLOADED)
                .subject(subject)
                .body(body)
                .status(NotificationStatus.PENDING)
                .build();
        notification = notificationRepository.save(notification);

        try {
            sendEmail(event.getActorEmail(), subject, body);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send document upload notification to {}: {}", event.getActorEmail(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }
        notificationRepository.save(notification);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
