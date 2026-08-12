package com.visaflow.common.exception;

import com.visaflow.modules.cases.entity.enums.CaseStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(CaseStatus from, CaseStatus to) {
        super("Invalid case status transition: " + from + " → " + to);
    }
}
