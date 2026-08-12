CREATE TABLE IF NOT EXISTS cases.case_sequences (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    year INT NOT NULL,
    visa_code VARCHAR(10) NOT NULL,
    sequence_value INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_case_sequences_company_year_visa UNIQUE (company_id, year, visa_code)
);
