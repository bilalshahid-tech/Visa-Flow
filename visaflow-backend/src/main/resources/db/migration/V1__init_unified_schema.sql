-- V1__init_unified_schema.sql
-- Consolidated Modular Monolith Database Schema for VisaFlow

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS cases;
CREATE SCHEMA IF NOT EXISTS documents;
CREATE SCHEMA IF NOT EXISTS risk;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS audit;

-- =============================================================================
-- 1. AUTH & USER DOMAIN (auth.*)
-- =============================================================================

CREATE TABLE auth.companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    subscription_plan VARCHAR(50),
    subscription_status VARCHAR(50),
    max_users INTEGER,
    max_cases INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    company_id UUID REFERENCES auth.companies(id) ON DELETE SET NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    account_locked BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth.email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth.login_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    login_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(20)
);

CREATE TABLE auth.invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 2. CASE DOMAIN (cases.*)
-- =============================================================================

CREATE TABLE cases.cases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    applicant_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
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

CREATE INDEX idx_cases_company_id ON cases.cases(company_id);
CREATE INDEX idx_cases_applicant_id ON cases.cases(applicant_id);

CREATE TABLE cases.case_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    old_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(255) NOT NULL
);

CREATE INDEX idx_status_history_case_id ON cases.case_status_history(case_id);

CREATE TABLE cases.case_timelines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID UNIQUE NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    initial_review_date TIMESTAMP,
    document_collection_deadline TIMESTAMP,
    embassy_submission_date TIMESTAMP,
    interview_date TIMESTAMP,
    expected_decision_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 3. DOCUMENT DOMAIN (documents.*)
-- =============================================================================

CREATE TABLE documents.documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    case_id UUID NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    original_filename VARCHAR(500) NOT NULL,
    stored_filename VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(200),
    document_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    rejection_reason TEXT,
    reviewed_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE documents.document_requirements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    case_type VARCHAR(100) NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    label VARCHAR(300) NOT NULL,
    is_mandatory BOOLEAN DEFAULT true,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_documents_case_id ON documents.documents(case_id);
CREATE INDEX idx_documents_company_id ON documents.documents(company_id);
CREATE INDEX idx_documents_status ON documents.documents(status);
CREATE INDEX idx_doc_requirements_case_type ON documents.document_requirements(company_id, case_type);

-- =============================================================================
-- 4. RISK DOMAIN (risk.*)
-- =============================================================================

CREATE TABLE risk.risk_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    case_id UUID NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    assessed_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    risk_score INTEGER NOT NULL DEFAULT 0,
    risk_level VARCHAR(50) NOT NULL DEFAULT 'LOW',
    score_breakdown JSONB,
    flags TEXT[],
    recommendation TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assessed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE risk.risk_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES auth.companies(id) ON DELETE CASCADE,
    rule_key VARCHAR(200) NOT NULL,
    description TEXT,
    score_impact INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    applies_to_case_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(rule_key)
);

CREATE TABLE risk.risk_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID NOT NULL REFERENCES risk.risk_assessments(id) ON DELETE CASCADE,
    flag_key VARCHAR(200) NOT NULL,
    flag_description TEXT,
    score_impact INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_risk_assessments_case_id ON risk.risk_assessments(case_id);
CREATE INDEX idx_risk_assessments_company_id ON risk.risk_assessments(company_id);
CREATE INDEX idx_risk_assessments_risk_level ON risk.risk_assessments(risk_level);

-- =============================================================================
-- 5. NOTIFICATION DOMAIN (notification.*)
-- =============================================================================

CREATE TABLE notification.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    case_id UUID NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255),
    notification_type VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
    subject VARCHAR(500),
    body TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    error_message TEXT,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE notification.notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    template_key VARCHAR(200) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    body_template TEXT NOT NULL,
    channel VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(company_id, template_key)
);

CREATE INDEX idx_notifications_case_id ON notification.notifications(case_id);
CREATE INDEX idx_notifications_company_id ON notification.notifications(company_id);
CREATE INDEX idx_notifications_status ON notification.notifications(status);

-- =============================================================================
-- 6. AUDIT DOMAIN (audit.*)
-- Note: entity_id is polymorphic (no single SQL FK constraint). Indexed by (entity_type, entity_id).
-- =============================================================================

CREATE TABLE audit.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    actor_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    actor_type VARCHAR(100),
    source_service VARCHAR(100) NOT NULL DEFAULT 'visaflow-backend',
    payload JSONB,
    ip_address VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_company_id ON audit.audit_logs(company_id);
CREATE INDEX idx_audit_logs_entity_id ON audit.audit_logs(entity_id);
CREATE INDEX idx_audit_logs_entity_type ON audit.audit_logs(entity_type);
CREATE INDEX idx_audit_logs_event_type ON audit.audit_logs(event_type);
CREATE INDEX idx_audit_logs_created_at ON audit.audit_logs(created_at);
CREATE INDEX idx_audit_logs_actor_id ON audit.audit_logs(actor_id);
