package com.visaflow.modules.cases.service;

import com.visaflow.common.exception.InvalidStatusTransitionException;
import com.visaflow.modules.cases.entity.enums.CaseStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Server-side state machine for case status transitions.
 *
 * Allowed transitions:
 *   DRAFT         → DOCS_PENDING
 *   DOCS_PENDING  → UNDER_REVIEW, DRAFT
 *   UNDER_REVIEW  → SUBMITTED, DOCS_PENDING
 *   SUBMITTED     → APPROVED, REJECTED
 *   APPROVED      → CLOSED
 *   REJECTED      → CLOSED, DOCS_PENDING
 *   CLOSED        → (none)
 */
@Component
public class CaseStateMachine {

    private static final Map<CaseStatus, Set<CaseStatus>> ALLOWED = Map.of(
        CaseStatus.DRAFT,        EnumSet.of(CaseStatus.DOCS_PENDING),
        CaseStatus.DOCS_PENDING, EnumSet.of(CaseStatus.UNDER_REVIEW, CaseStatus.DRAFT),
        CaseStatus.UNDER_REVIEW, EnumSet.of(CaseStatus.SUBMITTED, CaseStatus.DOCS_PENDING),
        CaseStatus.SUBMITTED,    EnumSet.of(CaseStatus.APPROVED, CaseStatus.REJECTED),
        CaseStatus.APPROVED,     EnumSet.of(CaseStatus.CLOSED),
        CaseStatus.REJECTED,     EnumSet.of(CaseStatus.CLOSED, CaseStatus.DOCS_PENDING),
        CaseStatus.CLOSED,       EnumSet.noneOf(CaseStatus.class)
    );

    /**
     * Returns the list of allowed next statuses from a given status.
     */
    public List<CaseStatus> getAllowedTransitions(CaseStatus current) {
        return ALLOWED.getOrDefault(current, EnumSet.noneOf(CaseStatus.class))
                      .stream()
                      .sorted()
                      .collect(Collectors.toList());
    }

    /**
     * Validates that the transition from → to is allowed.
     * Throws InvalidStatusTransitionException if not.
     */
    public void validateTransition(CaseStatus from, CaseStatus to) {
        Set<CaseStatus> allowed = ALLOWED.getOrDefault(from, EnumSet.noneOf(CaseStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
