package com.visaflow.modules.document.service;

import com.visaflow.common.event.DocumentEvent;
import com.visaflow.common.storage.StorageService;
import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.cases.entity.DocumentRequirement;
import com.visaflow.modules.cases.repository.DocumentRequirementRepository;
import com.visaflow.modules.cases.repository.VisaCaseRepository;
import com.visaflow.modules.document.dto.DocumentReviewRequest;
import com.visaflow.modules.document.entity.Document;
import com.visaflow.modules.document.entity.enums.DocumentStatus;
import com.visaflow.modules.document.entity.enums.DocumentType;
import com.visaflow.modules.document.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"
    );

    private final DocumentRepository documentRepository;
    private final VisaCaseRepository caseRepository;
    private final DocumentRequirementRepository requirementRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // Upload against a checklist requirement (or ad-hoc when requirementId=null)
    // -------------------------------------------------------------------------

    @Transactional
    public Document uploadDocument(UUID caseId, UUID requirementId, MultipartFile file,
                                   UserPrincipal principal) throws IOException {
        // Tenant isolation: case must belong to principal's company
        var visaCase = caseRepository.findByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new AccessDeniedException("Case not found or access denied"));

        // Validate mime type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("File type not allowed. Supported: JPEG, PNG, GIF, WebP, PDF.");
        }

        // Validate size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 10 MB.");
        }

        // Derive document type from requirement if present
        DocumentRequirement req = null;
        if (requirementId != null) {
            req = requirementRepository.findById(requirementId)
                    .orElseThrow(() -> new EntityNotFoundException("Document requirement not found: " + requirementId));
        }

        String extension = getExtension(file.getOriginalFilename());
        String storageKey = String.format("companies/%s/cases/%s/%s%s",
                principal.getCompanyId(), caseId, UUID.randomUUID(), extension);

        storageService.upload(storageKey, file.getInputStream(), file.getSize(), mimeType);

        Document doc = Document.builder()
                .companyId(principal.getCompanyId())
                .caseId(caseId)
                .uploadedBy(principal.getUserId())
                .requirementId(req != null ? req.getId() : null)
                .originalFilename(file.getOriginalFilename())
                .storageKey(storageKey)
                .fileSize(file.getSize())
                .mimeType(mimeType)
                .documentType(req != null
                        ? DocumentType.valueOf(req.getDocumentClass())
                        : DocumentType.OTHER)
                .status(DocumentStatus.PENDING_REVIEW)
                .build();

        doc = documentRepository.save(doc);

        log.info("Document uploaded: id={} caseId={} key={} by={}", doc.getId(), caseId, storageKey, principal.getEmail());

        eventPublisher.publishEvent(new DocumentEvent(this, doc.getId(), caseId,
                principal.getCompanyId(), principal.getUserId(), principal.getEmail(),
                "DOCUMENT_UPLOADED", doc.getDocumentType().name(), DocumentStatus.PENDING_REVIEW.name()));

        return doc;
    }

    // -------------------------------------------------------------------------
    // View: returns a 5-minute pre-signed URL (tenant-isolated)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public String generateViewUrl(UUID caseId, UUID documentId, UserPrincipal principal) {
        caseRepository.findByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new AccessDeniedException("Case not found or access denied"));

        Document doc = documentRepository.findByIdAndCompanyId(documentId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));

        if (!doc.getCaseId().equals(caseId)) {
            throw new AccessDeniedException("Document does not belong to the specified case");
        }

        if (doc.getStorageKey() != null) {
            return storageService.generatePresignedUrl(doc.getStorageKey(), 5);
        }
        // Legacy local-disk upload: return the file path as-is (caller must handle)
        return doc.getFilePath();
    }

    // -------------------------------------------------------------------------
    // Review: approve or reject a specific document
    // -------------------------------------------------------------------------

    @Transactional
    public Document reviewDocument(UUID caseId, UUID documentId, DocumentReviewRequest request,
                                   UserPrincipal principal) {
        caseRepository.findByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new AccessDeniedException("Case not found or access denied"));

        Document doc = documentRepository.findByIdAndCompanyId(documentId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));

        doc.setStatus(DocumentStatus.valueOf(request.getStatus()));
        doc.setReviewerNotes(request.getReviewerNotes());
        doc.setRejectionReason(request.getStatus().equals("REJECTED") ? request.getReviewerNotes() : null);
        doc.setReviewedBy(principal.getUserId());
        doc.setReviewedAt(LocalDateTime.now());

        doc = documentRepository.save(doc);
        log.info("Document reviewed: id={} status={} by={}", documentId, request.getStatus(), principal.getEmail());

        eventPublisher.publishEvent(new DocumentEvent(this, doc.getId(), caseId,
                principal.getCompanyId(), principal.getUserId(), principal.getEmail(),
                "DOCUMENT_REVIEWED", doc.getDocumentType().name(), request.getStatus()));

        return doc;
    }

    // -------------------------------------------------------------------------
    // List documents for case  (kept for compatibility)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Document> listDocumentsForCase(UUID caseId, UserPrincipal principal) {
        caseRepository.findByIdAndCompanyId(caseId, principal.getCompanyId())
                .orElseThrow(() -> new AccessDeniedException("Case not found or access denied"));
        return documentRepository.findByCaseIdAndCompanyId(caseId, principal.getCompanyId(),
                org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
