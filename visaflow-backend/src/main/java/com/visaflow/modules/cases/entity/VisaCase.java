package com.visaflow.modules.cases.entity;

import com.visaflow.modules.cases.entity.enums.CaseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cases", schema = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_type_id", nullable = true)
    private VisaType visaType;

    /** New: points to the selected VisaProgram (null for cases created before migration) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_program_id", nullable = true)
    private VisaProgram visaProgram;

    /** New: the stage the case is currently in (null for pre-migration cases) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stage_id", nullable = true)
    private VisaProgramStage currentStage;

    /** New: drives conditional document requirements (e.g. TOURISM) */
    @Column(name = "job_role_category", length = 60)
    private String jobRoleCategory;

    @Column(name = "assigned_staff_id")
    private UUID assignedStaffId;

    @Column(name = "case_reference", nullable = false, unique = true, length = 50)
    private String caseReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CaseStatus status;

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

    @OneToMany(mappedBy = "visaCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaseNote> caseNotes = new ArrayList<>();

    @OneToMany(mappedBy = "visaCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("enteredAt ASC")
    @Builder.Default
    private List<CaseStageHistory> stageHistory = new ArrayList<>();
}
