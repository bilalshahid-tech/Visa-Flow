package com.visaflow.modules.cases.controller;

import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.cases.dto.*;
import com.visaflow.modules.cases.entity.enums.CaseStatus;
import com.visaflow.modules.cases.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @PostMapping
    public ResponseEntity<CaseResponse> createCase(
            @Valid @RequestBody CreateCaseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caseService.createCase(request, principal));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<CaseDetailResponse> getCaseDetail(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(caseService.getCaseDetail(caseId, principal));
    }

    @GetMapping
    public ResponseEntity<Page<CaseResponse>> listCases(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) CaseStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(caseService.listCases(principal, status, pageable));
    }

    @PatchMapping("/{caseId}/status")
    public ResponseEntity<CaseDetailResponse> transitionStatus(
            @PathVariable UUID caseId,
            @Valid @RequestBody StatusTransitionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(caseService.transitionStatus(caseId, request, principal));
    }

    @GetMapping("/{caseId}/allowed-transitions")
    public ResponseEntity<List<CaseStatus>> allowedTransitions(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(caseService.getAllowedTransitions(caseId, principal));
    }

    @PostMapping("/{caseId}/notes")
    public ResponseEntity<CaseDetailResponse.NoteResponse> addNote(
            @PathVariable UUID caseId,
            @Valid @RequestBody AddNoteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caseService.addNote(caseId, request, principal));
    }
}
