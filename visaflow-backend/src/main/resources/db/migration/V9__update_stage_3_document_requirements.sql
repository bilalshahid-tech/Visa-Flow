-- V9__update_stage_3_document_requirements.sql
-- Updates Stage 3 (Visa File) document requirements:
--   - Removes APPOINTMENT_LETTER (retaining VFS_APPOINTMENT_LETTER)
--   - Adds VISA_APPLICATION_FORM (mandatory)
--   - Adds AIP_LETTER (Approval in Principle Letter, mandatory)
--   - Adds RECOMMENDATION_LETTER (optional)

-- Clear existing Stage 3 document requirements for Malta Work Visa stage 3
DELETE FROM cases.stage_document_requirements
WHERE stage_id = 'a1000001-0000-0000-0000-000000000003';

-- Re-insert updated Stage 3 document requirements
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

    ('a1000001-0000-0000-0000-000000000003', 'VFS_APPOINTMENT_LETTER',
     'VFS Appointment Letter',              TRUE, NULL, NULL, NULL, NULL, 4),

    ('a1000001-0000-0000-0000-000000000003', 'VISA_APPLICATION_FORM',
     'Visa Application Form',               TRUE, NULL, NULL, NULL, NULL, 5),

    ('a1000001-0000-0000-0000-000000000003', 'AIP_LETTER',
     'Approval in Principle (AIP) Letter',  TRUE, NULL, NULL, NULL, NULL, 6),

    ('a1000001-0000-0000-0000-000000000003', 'EMPLOYER_INVITATION_LETTER',
     'Employer-Signed Invitation Letter',   TRUE, NULL, NULL, NULL, NULL, 7),

    ('a1000001-0000-0000-0000-000000000003', 'RECOMMENDATION_LETTER',
     'Recommendation Letter',               FALSE, NULL, NULL, NULL, 'Optional supporting document', 8),

    ('a1000001-0000-0000-0000-000000000003', 'SKILLS_PASS_PROOF',
     'Skills Pass Proof (Tourism roles)',   FALSE,
     'job_role_category', 'EQUALS', 'TOURISM',
     'Required for Tourism job role category cases only.',
     9);
