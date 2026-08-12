package com.visaflow.modules.risk.controller;

import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.risk.entity.RiskAssessment;
import com.visaflow.modules.risk.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @PostMapping("/case/{caseId}/assess")
    public ResponseEntity<RiskAssessment> assess(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(riskService.assess(caseId, principal));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<RiskAssessment> getLatest(
            @PathVariable UUID caseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(riskService.getLatest(caseId, principal));
    }
}
