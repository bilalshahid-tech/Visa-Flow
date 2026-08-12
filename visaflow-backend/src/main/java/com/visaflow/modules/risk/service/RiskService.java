package com.visaflow.modules.risk.service;

import com.visaflow.modules.auth.security.UserPrincipal;
import com.visaflow.modules.risk.entity.RiskAssessment;
import com.visaflow.modules.risk.entity.enums.AssessmentStatus;
import com.visaflow.modules.risk.entity.enums.RiskLevel;
import com.visaflow.modules.risk.repository.RiskAssessmentRepository;
import com.visaflow.modules.document.entity.Document;
import com.visaflow.modules.document.entity.enums.DocumentStatus;
import com.visaflow.modules.document.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskAssessmentRepository riskRepository;
    private final DocumentRepository documentRepository;

    /**
     * Runs a heuristic risk assessment for a case based on document completeness.
     * Called automatically on case creation or triggered manually.
     */
    @Transactional
    public RiskAssessment assess(UUID caseId, UserPrincipal principal) {
        List<Document> docs = documentRepository
                .findByCaseIdAndCompanyIdAndStatus(caseId, principal.getCompanyId(), DocumentStatus.PENDING_REVIEW);

        Map<String, Integer> breakdown = new HashMap<>();
        int score = 0;

        int pending = docs.size();
        breakdown.put("pending_documents", pending);
        score += pending * 10; // each pending document adds 10 risk points

        RiskLevel level;
        if (score <= 20) level = RiskLevel.LOW;
        else if (score <= 50) level = RiskLevel.MEDIUM;
        else if (score <= 80) level = RiskLevel.HIGH;
        else level = RiskLevel.CRITICAL;

        RiskAssessment assessment = RiskAssessment.builder()
                .companyId(principal.getCompanyId())
                .caseId(caseId)
                .riskScore(score)
                .riskLevel(level)
                .scoreBreakdown(breakdown)
                .recommendation(level == RiskLevel.LOW
                        ? "Case looks complete. Ready for submission review."
                        : "Action required: " + pending + " document(s) still pending review.")
                .status(AssessmentStatus.COMPLETED)
                .assessedAt(LocalDateTime.now())
                .build();

        return riskRepository.save(assessment);
    }

    @Transactional(readOnly = true)
    public RiskAssessment getLatest(UUID caseId, UserPrincipal principal) {
        return riskRepository.findTopByCaseIdAndCompanyIdOrderByCreatedAtDesc(caseId, principal.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No risk assessment found for case: " + caseId));
    }
}
