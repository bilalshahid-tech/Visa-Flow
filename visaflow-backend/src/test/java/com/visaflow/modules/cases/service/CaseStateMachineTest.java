package com.visaflow.modules.cases.service;

import com.visaflow.common.exception.InvalidStatusTransitionException;
import com.visaflow.modules.cases.entity.enums.CaseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CaseStateMachineTest {

    private CaseStateMachine machine;

    @BeforeEach
    void setUp() {
        machine = new CaseStateMachine();
    }

    // ---- Valid transitions ----

    @Test void draft_to_docsPending_isValid() {
        assertThatNoException().isThrownBy(() -> machine.validateTransition(CaseStatus.DRAFT, CaseStatus.DOCS_PENDING));
    }

    @Test void docsPending_to_underReview_isValid() {
        assertThatNoException().isThrownBy(() -> machine.validateTransition(CaseStatus.DOCS_PENDING, CaseStatus.UNDER_REVIEW));
    }

    @Test void docsPending_back_to_draft_isValid() {
        assertThatNoException().isThrownBy(() -> machine.validateTransition(CaseStatus.DOCS_PENDING, CaseStatus.DRAFT));
    }

    @Test void underReview_to_submitted_isValid() {
        assertThatNoException().isThrownBy(() -> machine.validateTransition(CaseStatus.UNDER_REVIEW, CaseStatus.SUBMITTED));
    }

    @Test void submitted_to_approved_isValid() {
        assertThatNoException().isThrownBy(() -> machine.validateTransition(CaseStatus.SUBMITTED, CaseStatus.APPROVED));
    }

    @Test void submitted_to_rejected_isValid() {
        assertThatNoException().isThrownBy(() -> machine.validateTransition(CaseStatus.SUBMITTED, CaseStatus.REJECTED));
    }

    @Test void approved_to_closed_isValid() {
        assertThatNoException().isThrownBy(() -> machine.validateTransition(CaseStatus.APPROVED, CaseStatus.CLOSED));
    }

    @Test void rejected_to_docsPending_isValid() {
        assertThatNoException().isThrownBy(() -> machine.validateTransition(CaseStatus.REJECTED, CaseStatus.DOCS_PENDING));
    }

    // ---- Invalid transitions ----

    @Test void draft_to_approved_isInvalid() {
        assertThatThrownBy(() -> machine.validateTransition(CaseStatus.DRAFT, CaseStatus.APPROVED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test void draft_to_submitted_isInvalid() {
        assertThatThrownBy(() -> machine.validateTransition(CaseStatus.DRAFT, CaseStatus.SUBMITTED))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test void closed_to_draft_isInvalid() {
        assertThatThrownBy(() -> machine.validateTransition(CaseStatus.CLOSED, CaseStatus.DRAFT))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test void approved_to_rejected_isInvalid() {
        assertThatThrownBy(() -> machine.validateTransition(CaseStatus.APPROVED, CaseStatus.REJECTED))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // ---- getAllowedTransitions ----

    @Test void draft_allowedTransitions_containsDocsPending() {
        List<CaseStatus> allowed = machine.getAllowedTransitions(CaseStatus.DRAFT);
        assertThat(allowed).containsExactly(CaseStatus.DOCS_PENDING);
    }

    @Test void closed_allowedTransitions_isEmpty() {
        List<CaseStatus> allowed = machine.getAllowedTransitions(CaseStatus.CLOSED);
        assertThat(allowed).isEmpty();
    }

    @Test void submitted_allowedTransitions_containsApprovedAndRejected() {
        List<CaseStatus> allowed = machine.getAllowedTransitions(CaseStatus.SUBMITTED);
        assertThat(allowed).containsExactlyInAnyOrder(CaseStatus.APPROVED, CaseStatus.REJECTED);
    }
}
