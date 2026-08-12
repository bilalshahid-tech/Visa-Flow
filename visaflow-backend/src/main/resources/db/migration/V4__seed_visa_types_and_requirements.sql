-- V4__seed_visa_types_and_requirements.sql
-- Seeds visa_types and document_requirements master data

-- =============================================================================
-- 1. VISA TYPES
-- =============================================================================

INSERT INTO cases.visa_types (id, code, name, country) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'WORK',      'Work / Employment Visa',         'Multiple Countries'),
    ('a1000000-0000-0000-0000-000000000002', 'STUDY',     'Student / Study Visa',           'Multiple Countries'),
    ('a1000000-0000-0000-0000-000000000003', 'TOURIST',   'Tourist / Holiday Visa',         'Multiple Countries'),
    ('a1000000-0000-0000-0000-000000000004', 'VISIT',     'Family / Friend Visit Visa',     'Multiple Countries'),
    ('a1000000-0000-0000-0000-000000000005', 'BUSINESS',  'Business / Investor Visa',       'Multiple Countries'),
    ('a1000000-0000-0000-0000-000000000006', 'FAMILY',    'Family Reunification Visa',      'Multiple Countries'),
    ('a1000000-0000-0000-0000-000000000007', 'MEDICAL',   'Medical Treatment Visa',         'Multiple Countries'),
    ('a1000000-0000-0000-0000-000000000008', 'PERMANENT', 'Permanent Residency Application','Multiple Countries');

-- =============================================================================
-- 2. DOCUMENT REQUIREMENTS (per visa type)
--    document_class values align with DocumentClass enum in Java
-- =============================================================================

-- WORK VISA requirements
INSERT INTO cases.document_requirements (visa_type_id, document_class, label, is_mandatory, display_order) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'PASSPORT_BIO',           'Passport Bio Page (valid 6+ months)',         TRUE,  1),
    ('a1000000-0000-0000-0000-000000000001', 'PASSPORT_PHOTO',         'Recent Passport-Sized Photograph',            TRUE,  2),
    ('a1000000-0000-0000-0000-000000000001', 'EMPLOYMENT_CONTRACT',    'Signed Employment Contract / Offer Letter',   TRUE,  3),
    ('a1000000-0000-0000-0000-000000000001', 'POLICE_CLEARANCE',       'Police Clearance Certificate',                TRUE,  4),
    ('a1000000-0000-0000-0000-000000000001', 'MEDICAL_CERTIFICATE',    'Medical Fitness Certificate',                 TRUE,  5),
    ('a1000000-0000-0000-0000-000000000001', 'BANK_STATEMENT',         'Bank Statement (last 3 months)',               FALSE, 6),
    ('a1000000-0000-0000-0000-000000000001', 'COVER_LETTER',           'Cover Letter / Personal Statement',           FALSE, 7);

-- STUDY VISA requirements
INSERT INTO cases.document_requirements (visa_type_id, document_class, label, is_mandatory, display_order) VALUES
    ('a1000000-0000-0000-0000-000000000002', 'PASSPORT_BIO',           'Passport Bio Page (valid 6+ months)',         TRUE,  1),
    ('a1000000-0000-0000-0000-000000000002', 'PASSPORT_PHOTO',         'Recent Passport-Sized Photograph',            TRUE,  2),
    ('a1000000-0000-0000-0000-000000000002', 'EDUCATION_CERTIFICATE',  'Previous Education Certificates / Transcripts',TRUE, 3),
    ('a1000000-0000-0000-0000-000000000002', 'BANK_STATEMENT',         'Financial Sponsorship Proof / Bank Statement', TRUE,  4),
    ('a1000000-0000-0000-0000-000000000002', 'POLICE_CLEARANCE',       'Police Clearance Certificate',                TRUE,  5),
    ('a1000000-0000-0000-0000-000000000002', 'COVER_LETTER',           'Personal Statement / Cover Letter',           TRUE,  6),
    ('a1000000-0000-0000-0000-000000000002', 'MEDICAL_CERTIFICATE',    'Medical Fitness Certificate',                 FALSE, 7);

-- TOURIST VISA requirements
INSERT INTO cases.document_requirements (visa_type_id, document_class, label, is_mandatory, display_order) VALUES
    ('a1000000-0000-0000-0000-000000000003', 'PASSPORT_BIO',           'Passport Bio Page (valid 6+ months)',         TRUE,  1),
    ('a1000000-0000-0000-0000-000000000003', 'PASSPORT_PHOTO',         'Recent Passport-Sized Photograph',            TRUE,  2),
    ('a1000000-0000-0000-0000-000000000003', 'BANK_STATEMENT',         'Bank Statement (last 3 months)',               TRUE,  3),
    ('a1000000-0000-0000-0000-000000000003', 'COVER_LETTER',           'Travel Itinerary / Purpose of Visit Letter',  FALSE, 4);

-- VISIT VISA requirements
INSERT INTO cases.document_requirements (visa_type_id, document_class, label, is_mandatory, display_order) VALUES
    ('a1000000-0000-0000-0000-000000000004', 'PASSPORT_BIO',           'Passport Bio Page (valid 6+ months)',         TRUE,  1),
    ('a1000000-0000-0000-0000-000000000004', 'PASSPORT_PHOTO',         'Recent Passport-Sized Photograph',            TRUE,  2),
    ('a1000000-0000-0000-0000-000000000004', 'BANK_STATEMENT',         'Bank Statement (last 3 months)',               TRUE,  3),
    ('a1000000-0000-0000-0000-000000000004', 'COVER_LETTER',           'Invitation Letter from Host',                 TRUE,  4);

-- BUSINESS VISA requirements
INSERT INTO cases.document_requirements (visa_type_id, document_class, label, is_mandatory, display_order) VALUES
    ('a1000000-0000-0000-0000-000000000005', 'PASSPORT_BIO',           'Passport Bio Page (valid 6+ months)',         TRUE,  1),
    ('a1000000-0000-0000-0000-000000000005', 'PASSPORT_PHOTO',         'Recent Passport-Sized Photograph',            TRUE,  2),
    ('a1000000-0000-0000-0000-000000000005', 'BANK_STATEMENT',         'Business Financial Statements / Bank Proof',  TRUE,  3),
    ('a1000000-0000-0000-0000-000000000005', 'EMPLOYMENT_CONTRACT',    'Business Registration / Company Documents',   TRUE,  4),
    ('a1000000-0000-0000-0000-000000000005', 'COVER_LETTER',           'Business Purpose Letter',                     TRUE,  5),
    ('a1000000-0000-0000-0000-000000000005', 'POLICE_CLEARANCE',       'Police Clearance Certificate',                FALSE, 6);

-- FAMILY REUNIFICATION requirements
INSERT INTO cases.document_requirements (visa_type_id, document_class, label, is_mandatory, display_order) VALUES
    ('a1000000-0000-0000-0000-000000000006', 'PASSPORT_BIO',           'Passport Bio Page (valid 6+ months)',         TRUE,  1),
    ('a1000000-0000-0000-0000-000000000006', 'PASSPORT_PHOTO',         'Recent Passport-Sized Photograph',            TRUE,  2),
    ('a1000000-0000-0000-0000-000000000006', 'POLICE_CLEARANCE',       'Police Clearance Certificate',                TRUE,  3),
    ('a1000000-0000-0000-0000-000000000006', 'MEDICAL_CERTIFICATE',    'Medical Fitness Certificate',                 TRUE,  4),
    ('a1000000-0000-0000-0000-000000000006', 'BANK_STATEMENT',         'Proof of Family Relationship / Support Letter',TRUE, 5),
    ('a1000000-0000-0000-0000-000000000006', 'COVER_LETTER',           'Cover Letter / Sponsorship Declaration',      FALSE, 6);

-- MEDICAL VISA requirements
INSERT INTO cases.document_requirements (visa_type_id, document_class, label, is_mandatory, display_order) VALUES
    ('a1000000-0000-0000-0000-000000000007', 'PASSPORT_BIO',           'Passport Bio Page (valid 6+ months)',         TRUE,  1),
    ('a1000000-0000-0000-0000-000000000007', 'PASSPORT_PHOTO',         'Recent Passport-Sized Photograph',            TRUE,  2),
    ('a1000000-0000-0000-0000-000000000007', 'MEDICAL_CERTIFICATE',    'Medical Report / Hospital Appointment Letter', TRUE,  3),
    ('a1000000-0000-0000-0000-000000000007', 'BANK_STATEMENT',         'Proof of Funds for Medical Treatment',        TRUE,  4),
    ('a1000000-0000-0000-0000-000000000007', 'COVER_LETTER',           'Medical Treatment Purpose Letter',            FALSE, 5);

-- PERMANENT RESIDENCY requirements
INSERT INTO cases.document_requirements (visa_type_id, document_class, label, is_mandatory, display_order) VALUES
    ('a1000000-0000-0000-0000-000000000008', 'PASSPORT_BIO',           'Passport Bio Page (valid 6+ months)',         TRUE,  1),
    ('a1000000-0000-0000-0000-000000000008', 'PASSPORT_PHOTO',         'Recent Passport-Sized Photograph',            TRUE,  2),
    ('a1000000-0000-0000-0000-000000000008', 'POLICE_CLEARANCE',       'Police Clearance Certificate',                TRUE,  3),
    ('a1000000-0000-0000-0000-000000000008', 'MEDICAL_CERTIFICATE',    'Full Medical Examination Certificate',        TRUE,  4),
    ('a1000000-0000-0000-0000-000000000008', 'BANK_STATEMENT',         'Bank Statement (last 6 months)',               TRUE,  5),
    ('a1000000-0000-0000-0000-000000000008', 'EMPLOYMENT_CONTRACT',    'Employment / Income Proof',                   TRUE,  6),
    ('a1000000-0000-0000-0000-000000000008', 'EDUCATION_CERTIFICATE',  'Educational Qualifications',                  FALSE, 7),
    ('a1000000-0000-0000-0000-000000000008', 'COVER_LETTER',           'Personal Statement / Cover Letter',           TRUE,  8);
