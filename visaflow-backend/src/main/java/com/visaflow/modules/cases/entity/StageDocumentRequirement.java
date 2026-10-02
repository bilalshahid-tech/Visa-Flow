package com.visaflow.modules.cases.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "stage_document_requirements", schema = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageDocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private VisaProgramStage stage;

    /** Controlled value, e.g. PASSPORT_BIO, CV, FORM_C5 */
    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    /** Human-readable label shown in the UI */
    @Column(name = "display_name", nullable = false, length = 300)
    private String displayName;

    /** Base mandatory flag (may be overridden to true by conditional evaluation) */
    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory;

    /** Case attribute field to evaluate, e.g. "job_role_category". Null = unconditional. */
    @Column(name = "conditional_field", length = 100)
    private String conditionalField;

    /** Operator for condition, currently only EQUALS is supported */
    @Column(name = "conditional_operator", length = 30)
    private String conditionalOperator;

    /** Expected value for the condition, e.g. "TOURISM" */
    @Column(name = "conditional_value", length = 100)
    private String conditionalValue;

    /** Optional notes shown to the admin, e.g. "Must be issued within 3 months" */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
