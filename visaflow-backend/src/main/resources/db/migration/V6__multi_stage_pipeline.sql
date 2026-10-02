-- V6__multi_stage_pipeline.sql
-- Creates the multi-stage case pipeline schema:
--   countries, visa_categories, visa_programs, visa_program_stages,
--   stage_document_requirements, case_stage_history
-- Alters: cases.cases (add visa_program_id, current_stage_id, job_role_category)
--         documents.documents (add stage_document_requirement_id)

-- =============================================================================
-- 1. COUNTRIES
-- =============================================================================

CREATE TABLE cases.countries (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name     VARCHAR(150) NOT NULL,
    iso_code VARCHAR(5)   NOT NULL UNIQUE
);

-- =============================================================================
-- 2. VISA CATEGORIES
-- =============================================================================

CREATE TABLE cases.visa_categories (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE
);

-- =============================================================================
-- 3. VISA PROGRAMS (country + category → named program)
-- =============================================================================

CREATE TABLE cases.visa_programs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id       UUID         NOT NULL REFERENCES cases.countries(id)       ON DELETE RESTRICT,
    visa_category_id UUID         NOT NULL REFERENCES cases.visa_categories(id) ON DELETE RESTRICT,
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_visa_programs_country_id ON cases.visa_programs(country_id);
CREATE INDEX idx_visa_programs_active     ON cases.visa_programs(is_active);

-- =============================================================================
-- 4. VISA PROGRAM STAGES (ordered stages within a program)
-- =============================================================================

CREATE TABLE cases.visa_program_stages (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    visa_program_id  UUID         NOT NULL REFERENCES cases.visa_programs(id) ON DELETE CASCADE,
    sequence_order   INT          NOT NULL,
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    UNIQUE (visa_program_id, sequence_order)
);

CREATE INDEX idx_vps_program_id ON cases.visa_program_stages(visa_program_id);

-- =============================================================================
-- 5. STAGE DOCUMENT REQUIREMENTS
-- =============================================================================

CREATE TABLE cases.stage_document_requirements (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_id             UUID         NOT NULL REFERENCES cases.visa_program_stages(id) ON DELETE CASCADE,
    document_type        VARCHAR(100) NOT NULL,   -- controlled value, e.g. PASSPORT_BIO, CV
    display_name         VARCHAR(300) NOT NULL,   -- human-readable label shown in UI
    is_mandatory         BOOLEAN      NOT NULL DEFAULT TRUE,
    conditional_field    VARCHAR(100),             -- e.g. 'job_role_category'
    conditional_operator VARCHAR(30),              -- e.g. 'EQUALS'
    conditional_value    VARCHAR(100),             -- e.g. 'TOURISM'
    notes                TEXT,                     -- e.g. 'Must be issued within 3 months'
    display_order        INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_sdr_stage_id ON cases.stage_document_requirements(stage_id);

-- =============================================================================
-- 6. CASE STAGE HISTORY (audit trail of stage progression per case)
-- =============================================================================

CREATE TABLE cases.case_stage_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id      UUID      NOT NULL REFERENCES cases.cases(id)              ON DELETE CASCADE,
    stage_id     UUID      NOT NULL REFERENCES cases.visa_program_stages(id) ON DELETE RESTRICT,
    entered_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    completed_by UUID      REFERENCES auth.users(id) ON DELETE SET NULL
);

CREATE INDEX idx_csh_case_id  ON cases.case_stage_history(case_id);
CREATE INDEX idx_csh_stage_id ON cases.case_stage_history(stage_id);

-- =============================================================================
-- 7. ALTER cases.cases — add visa_program_id, current_stage_id, job_role_category
--    (nullable: old cases remain valid; new cases must supply these via app layer)
-- =============================================================================

ALTER TABLE cases.cases
    ADD COLUMN IF NOT EXISTS visa_program_id  UUID REFERENCES cases.visa_programs(id)       ON DELETE RESTRICT,
    ADD COLUMN IF NOT EXISTS current_stage_id UUID REFERENCES cases.visa_program_stages(id) ON DELETE RESTRICT,
    ADD COLUMN IF NOT EXISTS job_role_category VARCHAR(60);

CREATE INDEX IF NOT EXISTS idx_cases_visa_program_id  ON cases.cases(visa_program_id);
CREATE INDEX IF NOT EXISTS idx_cases_current_stage_id ON cases.cases(current_stage_id);

-- =============================================================================
-- 8. ALTER documents.documents — add stage_document_requirement_id
--    (nullable: old uploads keep their requirement_id; new uploads use this column)
-- =============================================================================

ALTER TABLE documents.documents
    ADD COLUMN IF NOT EXISTS stage_document_requirement_id
        UUID REFERENCES cases.stage_document_requirements(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_docs_stage_req_id ON documents.documents(stage_document_requirement_id);
