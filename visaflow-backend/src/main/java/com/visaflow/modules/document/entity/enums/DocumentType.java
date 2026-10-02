package com.visaflow.modules.document.entity.enums;

public enum DocumentType {
    // ── Values that match documentClass in document_requirements (seed data) ──
    PASSPORT_BIO,           // Passport bio/data page
    PASSPORT_PHOTO,         // Passport-sized photograph
    EMPLOYMENT_CONTRACT,    // Employment contract / offer letter
    POLICE_CLEARANCE,       // Police clearance certificate
    MEDICAL_CERTIFICATE,    // Medical fitness / examination certificate
    BANK_STATEMENT,         // Bank statement / financial proof
    COVER_LETTER,           // Cover letter / personal statement
    EDUCATION_CERTIFICATE,  // Educational qualifications / transcripts

    // ── Stage-specific document types ───────────────────────────────────────
    PASSPORT_FULL_COPY,
    SKILLS_PASS_PHASE1_PROOF,
    FORM_C5,
    CERTIFICATIONS,
    EDUCATION_DOCUMENTS,
    TRAVEL_INSURANCE,
    HOTEL_BOOKING,
    DUMMY_TICKET,
    VFS_APPOINTMENT_LETTER,
    VISA_APPLICATION_FORM,
    AIP_LETTER,
    EMPLOYER_INVITATION_LETTER,
    RECOMMENDATION_LETTER,
    SKILLS_PASS_PROOF,

    // ── Legacy / additional values kept for backward compatibility ────────────
    PASSPORT, BIRTH_CERTIFICATE, EMPLOYMENT_LETTER, INVITATION_LETTER,
    TRAVEL_HISTORY, PHOTO, TAX_RETURNS, MARRIAGE_CERTIFICATE,
    EDUCATIONAL_CERTIFICATE, APPOINTMENT_LETTER, OTHER
}
