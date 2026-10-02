package com.visaflow.modules.document.service;

import com.visaflow.common.event.DocumentEvent;
import com.visaflow.common.storage.StorageService;
import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.cases.entity.DocumentRequirement;
import com.visaflow.modules.cases.entity.StageDocumentRequirement;
import com.visaflow.modules.cases.repository.DocumentRequirementRepository;
import com.visaflow.modules.cases.repository.StageDocumentRequirementRepository;
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

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"
    );

    private final DocumentRepository documentRepository;
    private final VisaCaseRepository caseRepository;
    private final DocumentRequirementRepository requirementRepository;
    private final StageDocumentRequirementRepository stageRequirementRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // Upload against a checklist requirement (or ad-hoc when requirementId=null)
    // -------------------------------------------------------------------------

    @Transactional
    public Document uploadDocument(UUID caseId, UUID requirementId, UUID stageDocumentRequirementId,
                                   MultipartFile file, UserPrincipal principal) throws IOException {
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
            throw new IllegalArgumentException("File exceeds maximum allowed size of 50 MB.");
        }

        // Resolve legacy flat requirement (if provided)
        DocumentRequirement req = null;
        if (requirementId != null) {
            req = requirementRepository.findById(requirementId)
                    .orElseThrow(() -> new EntityNotFoundException("Document requirement not found: " + requirementId));
        }

        // Resolve new stage requirement (if provided)
        StageDocumentRequirement stageReq = null;
        if (stageDocumentRequirementId != null) {
            stageReq = stageRequirementRepository.findById(stageDocumentRequirementId)
                    .orElseThrow(() -> new EntityNotFoundException("Stage document requirement not found: " + stageDocumentRequirementId));
        }

        String extension = getExtension(file.getOriginalFilename());
        String rawFilename = UUID.randomUUID() + extension;
        String storageKey = String.format("companies/%s/cases/%s/%s",
                principal.getCompanyId(), caseId, rawFilename);

        storageService.upload(storageKey, file.getInputStream(), file.getSize(), mimeType);

        // Determine document type: prefer stageReq, then legacy req, then OTHER
        DocumentType docType = DocumentType.OTHER;
        if (stageReq != null) {
            docType = safeDocumentType(stageReq.getDocumentType());
        } else if (req != null) {
            docType = safeDocumentType(req.getDocumentClass());
        }

        Document doc = Document.builder()
                .companyId(principal.getCompanyId())
                .caseId(caseId)
                .uploadedBy(principal.getUserId())
                .requirementId(req != null ? req.getId() : null)
                .stageDocumentRequirementId(stageReq != null ? stageReq.getId() : null)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(rawFilename)
                .filePath(storageKey)
                .storageKey(storageKey)
                .fileSize(file.getSize())
                .mimeType(mimeType)
                .documentType(docType)
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

    /** Converts a documentClass string to a DocumentType enum, falling back to OTHER on unknown values. */
    private DocumentType safeDocumentType(String documentClass) {
        if (documentClass == null) return DocumentType.OTHER;
        try {
            return DocumentType.valueOf(documentClass);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown documentClass '{}' — defaulting to DocumentType.OTHER", documentClass);
            return DocumentType.OTHER;
        }
    }
}
