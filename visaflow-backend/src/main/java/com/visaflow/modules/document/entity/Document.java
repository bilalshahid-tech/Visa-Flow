package com.visaflow.modules.document.entity;

import com.visaflow.modules.document.entity.enums.DocumentStatus;
import com.visaflow.modules.document.entity.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents", schema = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    /** FK to cases.document_requirements — null means ad-hoc upload */
    @Column(name = "requirement_id")
    private UUID requirementId;

    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename;

    /** Legacy local-disk path — kept for old uploads, null for MinIO uploads */
    @Column(name = "stored_filename", length = 500)
    private String storedFilename;

    /** Legacy local-disk path — null for MinIO uploads */
    @Column(name = "file_path", length = 1000)
    private String filePath;

    /** MinIO object key — null for legacy local-disk uploads */
    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 200)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 100)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING_REVIEW;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /** Notes from the reviewer (separate from rejection reason) */
    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
