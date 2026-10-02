package com.visaflow.modules.cases.controller;

import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.cases.dto.*;
import com.visaflow.modules.cases.entity.Country;
import com.visaflow.modules.cases.entity.VisaProgram;
import com.visaflow.modules.cases.entity.enums.CaseStatus;
import com.visaflow.modules.cases.service.CaseService;
import com.visaflow.modules.cases.service.CountryProgramService;
import com.visaflow.modules.cases.service.ProgramStageService;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final ProgramStageService programStageService;
    private final CountryProgramService countryProgramService;

    // -------------------------------------------------------------------------
    // Case CRUD
    // -------------------------------------------------------------------------

    @PostMapping("/cases")
    public ResponseEntity<CaseResponse> createCase(
            @Valid @RequestBody CreateCaseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caseService.createCase(request, principal));
    }

    @GetMapping("/cases/{caseId}")
    public ResponseEntity<CaseDetailResponse> getCaseDetail(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(caseService.getCaseDetail(caseId, principal));
    }

    @GetMapping("/cases")
    public ResponseEntity<Page<CaseResponse>> listCases(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) CaseStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(caseService.listCases(principal, status, pageable));
    }

    @PatchMapping("/cases/{caseId}/status")
    public ResponseEntity<CaseDetailResponse> transitionStatus(
            @PathVariable UUID caseId,
            @Valid @RequestBody StatusTransitionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(caseService.transitionStatus(caseId, request, principal));
    }

    @GetMapping("/cases/{caseId}/allowed-transitions")
    public ResponseEntity<List<CaseStatus>> allowedTransitions(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(caseService.getAllowedTransitions(caseId, principal));
    }

    @PostMapping("/cases/{caseId}/notes")
    public ResponseEntity<CaseDetailResponse.NoteResponse> addNote(
            @PathVariable UUID caseId,
            @Valid @RequestBody AddNoteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caseService.addNote(caseId, request, principal));
    }

    // -------------------------------------------------------------------------
    // Stage checklist & advance (new)
    // -------------------------------------------------------------------------

    /**
     * Returns the evaluated checklist for a specific stage of a case.
     * Conditional requirements are resolved against case attributes.
     */
    @GetMapping("/cases/{caseId}/stages/{stageId}/checklist")
    public ResponseEntity<List<CaseDetailResponse.StageChecklistItemResponse>> getStageChecklist(
            @PathVariable UUID caseId,
            @PathVariable UUID stageId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(programStageService.buildStageChecklist(caseId, stageId, principal));
    }

    /**
     * Advances the case to the next stage.
     * Validates that all mandatory (incl. conditionally mandatory) documents are uploaded first.
     * Returns the updated case detail.
     */
    @PostMapping("/cases/{caseId}/advance-stage")
    public ResponseEntity<CaseDetailResponse> advanceStage(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        programStageService.advanceStage(caseId, principal);
        return ResponseEntity.ok(caseService.getCaseDetail(caseId, principal));
    }

    // -------------------------------------------------------------------------
    // Country & Visa Program catalog (new)
    // -------------------------------------------------------------------------

    /** Returns all countries that have at least one active visa program. */
    @GetMapping("/countries")
    public ResponseEntity<List<Map<String, Object>>> listCountries(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Country> countries = countryProgramService.getAllCountries();
        List<Map<String, Object>> result = countries.stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "name", c.getName(),
                        "isoCode", c.getIsoCode()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** Returns active visa programs, optionally filtered by countryId. */
    @GetMapping("/visa-programs")
    public ResponseEntity<List<VisaProgramDto>> listVisaPrograms(
            @RequestParam(required = false) UUID countryId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<VisaProgramDto> programs = countryId != null
                ? countryProgramService.getProgramsForCountry(countryId)
                : countryProgramService.getAllActivePrograms();
        return ResponseEntity.ok(programs);
    }
}
