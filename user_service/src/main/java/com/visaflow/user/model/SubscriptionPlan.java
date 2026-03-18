package com.visaflow.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 3)
    private String currency;

    @Column(name = "interval_type", nullable = false, length = 20)
    private String intervalType;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Column(name = "max_cases", nullable = false)
    private Integer maxCases;

    @Column(name = "max_documents", nullable = false)
    private Integer maxDocuments;

    @Column(name = "storage_gb", nullable = false)
    private Integer storageGb;

    @Column(name = "api_access")
    private Boolean apiAccess;

    @Column(name = "advanced_analytics")
    private Boolean advancedAnalytics;

    @Column(name = "priority_support")
    private Boolean prioritySupport;

    @Column(name = "custom_branding")
    private Boolean customBranding;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object features;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (currency == null) currency = "USD";
        if (apiAccess == null) apiAccess = false;
        if (advancedAnalytics == null) advancedAnalytics = false;
        if (prioritySupport == null) prioritySupport = false;
        if (customBranding == null) customBranding = false;
        if (isActive == null) isActive = true;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
