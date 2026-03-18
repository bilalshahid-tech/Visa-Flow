package com.visaflow.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(length = 100)
    private String position;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 20)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = "PENDING";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
