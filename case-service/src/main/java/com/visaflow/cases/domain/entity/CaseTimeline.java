package com.visaflow.cases.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_timelines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseTimeline {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private Case visaCase;

    @Column(name = "initial_review_date")
    private LocalDateTime initialReviewDate;

    @Column(name = "document_collection_deadline")
    private LocalDateTime documentCollectionDeadline;

    @Column(name = "embassy_submission_date")
    private LocalDateTime embassySubmissionDate;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @Column(name = "expected_decision_date")
    private LocalDateTime expectedDecisionDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
