-- V1__init_schema.sql

CREATE TABLE cases (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    applicant_id UUID NOT NULL,
    case_reference VARCHAR(50) UNIQUE NOT NULL,
    visa_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_stage VARCHAR(50) NOT NULL,
    submission_date TIMESTAMP,
    decision_date TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

CREATE INDEX idx_cases_company_id ON cases(company_id);
CREATE INDEX idx_cases_applicant_id ON cases(applicant_id);

CREATE TABLE case_status_history (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL,
    old_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(255) NOT NULL,
    CONSTRAINT fk_case_status_history_case FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE
);

CREATE INDEX idx_status_history_case_id ON case_status_history(case_id);

CREATE TABLE case_timelines (
    id UUID PRIMARY KEY,
    case_id UUID UNIQUE NOT NULL,
    initial_review_date TIMESTAMP,
    document_collection_deadline TIMESTAMP,
    embassy_submission_date TIMESTAMP,
    interview_date TIMESTAMP,
    expected_decision_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_timeline_case FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE
);
