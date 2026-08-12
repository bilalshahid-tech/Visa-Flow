package com.visaflow.modules.audit.controller;

import com.visaflow.modules.audit.entity.AuditLog;
import com.visaflow.modules.audit.repository.AuditLogRepository;
import com.visaflow.modules.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> search(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(
                auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(principal.getCompanyId(), pageable)
        );
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> searchByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(
                auditLogRepository.findByEntityOrderByCreatedAtDesc(
                        principal.getCompanyId(), entityType.toUpperCase(), entityId, pageable)
        );
    }
}
