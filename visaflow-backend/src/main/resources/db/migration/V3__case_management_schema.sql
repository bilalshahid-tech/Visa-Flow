-- V3__case_management_schema.sql
-- Production-grade case management schema rebuild

-- =============================================================================
-- 1. CLIENTS (applicant client profiles per consultancy/company)
-- =============================================================================

CREATE TABLE cases.clients (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id        UUID NOT NULL REFERENCES auth.companies(id) ON DELETE CASCADE,
    full_name         VARCHAR(255) NOT NULL,
    passport_number   VARCHAR(50)  NOT NULL,
    nationality       VARCHAR(100) NOT NULL,
    date_of_birth     DATE         NOT NULL,
    phone             VARCHAR(30),
    email             VARCHAR(255),
    address           TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (company_id, passport_number)
);

CREATE INDEX idx_clients_company_id     ON cases.clients(company_id);
CREATE INDEX idx_clients_passport       ON cases.clients(company_id, passport_number);
CREATE INDEX idx_clients_full_name      ON cases.clients(company_id, full_name);

-- =============================================================================
-- 2. VISA TYPES (master list, seeded in V4)
-- =============================================================================

CREATE TABLE cases.visa_types (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code    VARCHAR(30)  NOT NULL UNIQUE,
    name    VARCHAR(150) NOT NULL,
    country VARCHAR(100) NOT NULL
);

-- =============================================================================
-- 3. DOCUMENT REQUIREMENTS (per-visa-type checklist template)
-- =============================================================================

CREATE TABLE cases.document_requirements (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    visa_type_id   UUID         NOT NULL REFERENCES cases.visa_types(id) ON DELETE CASCADE,
    document_class VARCHAR(100) NOT NULL,
    label          VARCHAR(300) NOT NULL,
    is_mandatory   BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order  INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_doc_req_visa_type_id ON cases.document_requirements(visa_type_id);

-- =============================================================================
-- 4. ALTER cases.cases — add client, visa_type, assigned_staff FKs
-- =============================================================================

ALTER TABLE cases.cases
    ADD COLUMN client_id          UUID REFERENCES cases.clients(id) ON DELETE RESTRICT,
    ADD COLUMN visa_type_id       UUID REFERENCES cases.visa_types(id) ON DELETE RESTRICT,
    ADD COLUMN assigned_staff_id  UUID REFERENCES auth.users(id) ON DELETE SET NULL;

-- Migrate existing applicant_id-based rows before dropping (no real data yet, safe)
ALTER TABLE cases.cases DROP COLUMN IF EXISTS applicant_id;
ALTER TABLE cases.cases DROP COLUMN IF EXISTS current_stage;

CREATE INDEX idx_cases_client_id         ON cases.cases(client_id);
CREATE INDEX idx_cases_visa_type_id      ON cases.cases(visa_type_id);
CREATE INDEX idx_cases_assigned_staff_id ON cases.cases(assigned_staff_id);

-- Migrate ACTIVE status to DOCS_PENDING (approved approach)
UPDATE cases.cases SET status = 'DOCS_PENDING' WHERE status = 'ACTIVE';

-- =============================================================================
-- 5. ALTER cases.case_status_history — add changed_by FK, note column
-- =============================================================================

ALTER TABLE cases.case_status_history
    ADD COLUMN IF NOT EXISTS changed_by_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS note          TEXT;

CREATE INDEX IF NOT EXISTS idx_status_history_changed_by ON cases.case_status_history(changed_by_id);

-- =============================================================================
-- 6. CASE NOTES
-- =============================================================================

CREATE TABLE cases.case_notes (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id    UUID      NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    author_id  UUID      NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    body       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_case_notes_case_id ON cases.case_notes(case_id);

-- =============================================================================
-- 7. ALTER documents.documents — add requirement_id FK, storage_key
-- =============================================================================

ALTER TABLE documents.documents
    ADD COLUMN IF NOT EXISTS requirement_id UUID      REFERENCES cases.document_requirements(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS storage_key    VARCHAR(500),
    ADD COLUMN IF NOT EXISTS reviewer_notes TEXT;

CREATE INDEX IF NOT EXISTS idx_documents_requirement_id ON documents.documents(requirement_id);

-- =============================================================================
-- 8. DROP legacy tables no longer in spec
-- =============================================================================

DROP TABLE IF EXISTS cases.case_timelines CASCADE;
DROP TABLE IF EXISTS cases.case_sequences CASCADE;
