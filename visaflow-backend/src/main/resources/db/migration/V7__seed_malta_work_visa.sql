-- V7__seed_malta_work_visa.sql
-- Seeds Malta Single Permit (Work Visa) program with 3 stages and all document requirements.
--
-- Idempotent strategy:
--   - countries, visa_categories, visa_programs: ON CONFLICT DO NOTHING (have PK unique constraint)
--   - visa_program_stages: ON CONFLICT DO NOTHING (have PK unique constraint)
--   - stage_document_requirements: DELETE then INSERT — no unique key on (stage_id, document_type),
--     so ON CONFLICT DO NOTHING would not prevent duplicates on re-runs.
--
-- UUID key:
--   Country       c0000001-0000-0000-0000-000000000001  (c = valid hex)
--   VisaCategory  ca000001-0000-0000-0000-000000000001  (ca = valid hex)
--   VisaProgram   b0000001-0000-0000-0000-000000000001  (b0 = valid hex)
--   Stage 1       a1000001-0000-0000-0000-000000000001  (a1 = valid hex)
--   Stage 2       a1000001-0000-0000-0000-000000000002
--   Stage 3       a1000001-0000-0000-0000-000000000003

-- =============================================================================
-- 1. COUNTRY — Malta
-- =============================================================================
INSERT INTO cases.countries (id, name, iso_code) VALUES
    ('c0000001-0000-0000-0000-000000000001', 'Malta', 'MT')
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 2. VISA CATEGORY — Work
-- =============================================================================
INSERT INTO cases.visa_categories (id, name) VALUES
    ('ca000001-0000-0000-0000-000000000001', 'Work')
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 3. VISA PROGRAM — Malta Single Permit - Work Visa
-- =============================================================================
INSERT INTO cases.visa_programs (id, country_id, visa_category_id, name, description, is_active) VALUES
    ('b0000001-0000-0000-0000-000000000001',
     'c0000001-0000-0000-0000-000000000001',
     'ca000001-0000-0000-0000-000000000001',
     'Malta Single Permit - Work Visa',
     'Single Permit for employment in Malta, covering Skills Pass and Visa File stages.',
     TRUE)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 4. VISA PROGRAM STAGES (3 stages)
-- =============================================================================
INSERT INTO cases.visa_program_stages (id, visa_program_id, sequence_order, name, description) VALUES
    ('a1000001-0000-0000-0000-000000000001',
     'b0000001-0000-0000-0000-000000000001',
     1, 'Skills Pass & Job Analysis',
     'Initial stage: collect passport and CV to assess job role category and begin Skills Pass application.'),
    ('a1000001-0000-0000-0000-000000000002',
     'b0000001-0000-0000-0000-000000000001',
     2, 'Initial File',
     'Compile the full initial application file for Single Permit submission.'),
    ('a1000001-0000-0000-0000-000000000003',
     'b0000001-0000-0000-0000-000000000001',
     3, 'Visa File',
     'Visa appointment file: travel documents, insurance, and employer invitation letter. Tourism roles require additional Skills Pass proof.')
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 5–7. STAGE DOCUMENT REQUIREMENTS
--   DELETE existing rows for these 3 stage IDs first to prevent duplicate rows
--   on migration re-runs (stage_document_requirements has no unique key on
--   (stage_id, document_type), so ON CONFLICT does not help here).
-- =============================================================================
DELETE FROM cases.stage_document_requirements
WHERE stage_id IN (
    'a1000001-0000-0000-0000-000000000001',
    'a1000001-0000-0000-0000-000000000002',
    'a1000001-0000-0000-0000-000000000003'
);

-- =============================================================================
-- 5. STAGE 1 — Skills Pass & Job Analysis — Document Requirements
-- =============================================================================
INSERT INTO cases.stage_document_requirements
    (stage_id, document_type, display_name, is_mandatory, display_order) VALUES
    ('a1000001-0000-0000-0000-000000000001', 'PASSPORT_BIO',
     'Passport (Bio Page)',  TRUE, 1),
    ('a1000001-0000-0000-0000-000000000001', 'CV',
     'Curriculum Vitae (CV)', TRUE, 2);

-- =============================================================================
-- 6. STAGE 2 — Initial File — Document Requirements
-- =============================================================================
INSERT INTO cases.stage_document_requirements
    (stage_id, document_type, display_name, is_mandatory, display_order) VALUES
    ('a1000001-0000-0000-0000-000000000002', 'COVER_LETTER',
     'Cover Letter',                        TRUE, 1),
    ('a1000001-0000-0000-0000-000000000002', 'SKILLS_PASS_PHASE1_PROOF',
     'Skills Pass Phase 1 Proof',           TRUE, 2),
    ('a1000001-0000-0000-0000-000000000002', 'CV',
     'Curriculum Vitae (CV)',               TRUE, 3),
    ('a1000001-0000-0000-0000-000000000002', 'EDUCATION_DOCUMENTS',
     'Educational Documents',              TRUE, 4),
    ('a1000001-0000-0000-0000-000000000002', 'CERTIFICATIONS',
     'Professional Certifications',         TRUE, 5),
    ('a1000001-0000-0000-0000-000000000002', 'PASSPORT_FULL_COPY',
     'Passport (Full Copy)',                TRUE, 6),
    ('a1000001-0000-0000-0000-000000000002', 'FORM_C5',
     'Form C5',                             TRUE, 7),
    ('a1000001-0000-0000-0000-000000000002', 'EMPLOYMENT_CONTRACT',
     'Signed Job Contract',                 TRUE, 8);

-- =============================================================================
-- 7. STAGE 3 — Visa File — Document Requirements
--    Note: SKILLS_PASS_PROOF is conditional on job_role_category = TOURISM
-- =============================================================================
INSERT INTO cases.stage_document_requirements
    (stage_id, document_type, display_name, is_mandatory,
     conditional_field, conditional_operator, conditional_value,
     notes, display_order) VALUES
    ('a1000001-0000-0000-0000-000000000003', 'TRAVEL_INSURANCE',
     'Travel Insurance',                    TRUE, NULL, NULL, NULL, NULL, 1),
    ('a1000001-0000-0000-0000-000000000003', 'HOTEL_BOOKING',
     'Hotel Booking',                       TRUE, NULL, NULL, NULL, NULL, 2),
    ('a1000001-0000-0000-0000-000000000003', 'DUMMY_TICKET',
     'Dummy Ticket',                        TRUE, NULL, NULL, NULL, NULL, 3),
    ('a1000001-0000-0000-0000-000000000003', 'APPOINTMENT_LETTER',
     'Appointment Letter',                  TRUE, NULL, NULL, NULL, NULL, 4),
    ('a1000001-0000-0000-0000-000000000003', 'VFS_APPOINTMENT_LETTER',
     'VFS Appointment Letter',              TRUE, NULL, NULL, NULL, NULL, 5),
    ('a1000001-0000-0000-0000-000000000003', 'EMPLOYER_INVITATION_LETTER',
     'Employer-Signed Invitation Letter',   TRUE, NULL, NULL, NULL, NULL, 6),
    -- Conditional: mandatory ONLY when job_role_category = TOURISM
    ('a1000001-0000-0000-0000-000000000003', 'SKILLS_PASS_PROOF',
     'Skills Pass Proof (Tourism roles)',   FALSE,
     'job_role_category', 'EQUALS', 'TOURISM',
     'Required for Tourism job role category cases only.',
     7);
