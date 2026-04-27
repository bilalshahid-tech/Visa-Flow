package com.visaflow.cases.controller;

import com.visaflow.cases.dto.request.CreateCaseRequest;
import com.visaflow.cases.dto.request.UpdateCaseStatusRequest;
import com.visaflow.cases.dto.response.CaseResponse;
import com.visaflow.cases.dto.response.CaseStatusHistoryResponse;
import com.visaflow.cases.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_CONSULTANT', 'ROLE_ADMIN', 'ROLE_CONSULTANT')")
    public ResponseEntity<CaseResponse> createCase(
            @Valid @RequestBody CreateCaseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        UUID companyId = extractCompanyId(jwt);
        
        CaseResponse response = caseService.createCase(request, userId, companyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CaseResponse>> getCases(@AuthenticationPrincipal Jwt jwt) {
        UUID companyId = extractCompanyId(jwt);
        return ResponseEntity.ok(caseService.getCasesByUser(companyId));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<CaseResponse> getCaseById(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID companyId = extractCompanyId(jwt);
        return ResponseEntity.ok(caseService.getCaseById(caseId, companyId));
    }

    @PutMapping("/{caseId}/status")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_CONSULTANT', 'ROLE_ADMIN', 'ROLE_CONSULTANT')")
    public ResponseEntity<CaseResponse> updateCaseStatus(
            @PathVariable UUID caseId,
            @Valid @RequestBody UpdateCaseStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        UUID companyId = extractCompanyId(jwt);
        
        return ResponseEntity.ok(caseService.updateCaseStatus(caseId, request, userId, companyId));
    }

    @GetMapping("/{caseId}/history")
    public ResponseEntity<List<CaseStatusHistoryResponse>> getCaseHistory(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID companyId = extractCompanyId(jwt);
        return ResponseEntity.ok(caseService.getCaseHistory(caseId, companyId));
    }

    private UUID extractCompanyId(Jwt jwt) {
        String companyIdStr = jwt.getClaimAsString("company_id");
        if (companyIdStr == null || companyIdStr.isBlank()) {
            throw new IllegalArgumentException("company_id claim is missing from token");
        }
        return UUID.fromString(companyIdStr);
    }
}
