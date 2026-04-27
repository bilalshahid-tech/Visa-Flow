package com.visaflow.cases.domain.entity;

import com.visaflow.cases.domain.enums.CaseStage;
import com.visaflow.cases.domain.enums.CaseStatus;
import com.visaflow.cases.domain.enums.VisaType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Case {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "applicant_id", nullable = false)
    private UUID applicantId;

    @Column(name = "case_reference", nullable = false, unique = true, length = 50)
    private String caseReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "visa_type", nullable = false, length = 50)
    private VisaType visaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CaseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 50)
    private CaseStage currentStage;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @OneToMany(mappedBy = "visaCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaseStatusHistory> statusHistory = new ArrayList<>();

    @OneToOne(mappedBy = "visaCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private CaseTimeline timeline;
}
