# VisaFlow - Case Management Service

The Case Management Service is the core business logic hub of VisaFlow. It handles the lifecycle of Visa applications and ensures multi-tenant data separation.

## Key Features
- **Multi-tenant Strict Boundaries:** Parses `company_id` directly from `Jwt` tokens via the API Controller natively for boundary isolation.
- **Auditable Timelines:** Generates exact histories of Visa modifications stored via `CaseStatusHistory`.
- **Event-Driven Messaging:** Broadcasts all modifications instantly through Kafka payloads matching `CaseEventPayload.java` to `case-events`.
- **Robustness:** Native PostgreSQL DB scripts via Flyway.

## Dependencies
- Spring Boot Data JPA, Web, Security
- Flyway (PostgreSQL)
- Kafka Consumer/Publisher Logic
