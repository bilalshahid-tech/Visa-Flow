-- V8__deduplicate_stage_document_requirements.sql
-- Removes duplicate rows from cases.stage_document_requirements that were
-- created by multiple partial V7 runs (the table has no UNIQUE constraint on
-- (stage_id, document_type), so each V7 execution inserted fresh rows).
--
-- Strategy: for every (stage_id, document_type) group keep the single row
-- with the smallest id (first inserted) and delete all others.

DELETE FROM cases.stage_document_requirements
WHERE ctid NOT IN (
    SELECT DISTINCT ON (stage_id, document_type) ctid
    FROM cases.stage_document_requirements
    ORDER BY stage_id, document_type
);
