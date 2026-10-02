-- Flyway Migration V5: Drop legacy column visa_type from cases table
ALTER TABLE cases.cases DROP COLUMN IF EXISTS visa_type;
