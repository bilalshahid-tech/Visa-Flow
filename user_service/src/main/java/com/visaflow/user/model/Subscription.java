package com.visaflow.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "plan_id", nullable = false, length = 50)
    private String planId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "auto_renew")
    private Boolean autoRenew;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_method", columnDefinition = "jsonb")
    private Object paymentMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "billing_details", columnDefinition = "jsonb")
    private Object billingDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object metadata;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", insertable = false, updatable = false)
    private SubscriptionPlan plan;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = "ACTIVE";
        if (startDate == null) startDate = LocalDateTime.now();
        if (autoRenew == null) autoRenew = true;
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
