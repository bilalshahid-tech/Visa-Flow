# VisaFlow Microservices Platform

**VisaFlow** is a comprehensive B2B SaaS microservices platform custom-designed for Visa Consultancy Firms. It provides a robust architecture for managing clients, documentation workflows, case lifecycles, and risk profiles, all secured behind isolated multi-tenant parameters.

Built purely on **Spring Boot 3.4.1**, **PostgreSQL**, **Apache Kafka**, and **Docker**, secured by stateless **JWT (OAuth 2.0)** bounds.

## Services Architecture (ASCII)

```text
                                +-------------------+
                                |                   |
                                |    API Gateway    | (Port 8080)
                                |                   |
                                +---------+---------+
                                          |
        +---------------+-----------------+--------------+---------------+
        |               |                 |              |               |
+-------v-------+ +-----v-------+ +-------v------+ +-----v-------+ +-----v-------+
|  Auth Service | | User Service| | Case Service | | Document Srv| | Risk Service|
|  (Port 8081)  | | (Port 8082) | |  (Port 8084) | | (Port 8086) | | (Port 8087) |
+-------+-------+ +-----+-------+ +-------+------+ +-----+-------+ +-----+-------+
        |               |                 |              |               |
        +---------------+-----------------+--------------+---------------+
                                          |
                                   +------v------+
                                   |    Kafka    |  (Event Bus)
                                   +------+------+
                                          |
                           +--------------+---------------+
                           |                              |
                   +-------v-------+              +-------v-------+
                   |  Notification |              | Audit Service |
                   |  (Port 8085)  |              |  (Port 8088)  |
                   +---------------+              +---------------+
```

## Services Overview

| Service Name | Port | Database | Description |
|---|---|---|---|
| API Gateway | `8080` | N/A | Global routing engine mapping limits and enforcing standard paths across the cluster. |
| Auth Service | `8081` | `Visa-auth` | Issues stateless JWT/OAuth2 refresh tokens tracking scopes and User Roles (Admin/Consultant/Client). |
| User Service | `8082` | `visaflow_users` | Multi-tenant identity configurations and profile maps. |
| Case Service | `8084` | `visaflow_cases` | Core Case Lifecycle bounds. |
| Notification Service | `8085` | `visaflow_notifications` | Listens to all global events across the cluster natively and dispatches JavaMailSender streams securely. |
| Document Service | `8086` | `visaflow_documents` | File System mapping handling PDF workflows and approvals locally under isolated `/app/uploads`. |
| Risk Service | `8087` | `visaflow_risk` | AI heuristic tracking computing isolated risk algorithms automatically mapping heuristics. |
| Audit Service | `8088` | `visaflow_audit` | Immutable repository mapping immutable Event Sourcing tracks natively into `JSONB` structures limits securely. |

## Kafka Stream Topics

| Topic Name | Publisher | Consumers |
|---|---|---|
| `case-events` | `case-service` | `notification-service`, `risk-service`, `audit-service` |
| `document-events` | `document-service` | `notification-service`, `risk-service`, `audit-service` |
| `risk-events` | `risk-service` | `notification-service`, `audit-service` |

## Prerequisites

- **Java 21+**
- **Docker Engine & Docker Compose**

## How to Run

A single script command natively initializes Zookeeper, Kafka Brokers, PostgreSQL instances natively across 8 microservice boundaries securely!

```bash
docker-compose down -v
docker-compose up --build -d
```

Your Gateway maps securely over `http://localhost:8080`

*Critical note:* After running `docker-compose up`, ensure startup order initializes `postgres` -> `zookeeper` -> `kafka` -> `auth-service` sequentially before calling endpoints.

## Environment Variables

The Notification configuration hooks securely into standard SMTP constraints:

- `MAIL_USERNAME` : Host Email configurations.
- `MAIL_PASSWORD` : App-specific isolation generic configuration limits for external SMTP targets.

## API Endpoint References

| Service | Method | Route Path | Description | Authorization |
|---|---|---|---|---|
| **Auth** | POST | `/api/auth/login` | Authenticate boundaries natively generating tokens | None |
| **Auth** | POST | `/api/auth/register` | Open registration stream | None |
| **User** | GET | `/api/users/profile` | View Multi-tenant Profiles | REQUIRED (Any role) |
| **Case** | POST | `/api/cases` | Boot Case limits | ADMIN / CONSULTANT |
| **Case** | GET | `/api/cases/{caseId}` | View bounds mapping into isolation lists | REQUIRED (Any role) |
| **Document** | POST | `/api/documents/upload` | File streaming target | REQUIRED (Any role) |
| **Document** | PUT | `/api/documents/{id}/review` | Workflow metric transitions | REQUIRED (Any role) |
| **Risk** | GET | `/api/risk/case/{caseId}` | Read isolated AI risk heuristics | ADMIN / CONSULTANT |
| **Audit** | GET | `/api/audit/search` | Read generic immutable traces dynamically | ADMIN |

## Multi-Tenancy Design

Multi-tenancy boundaries are enforced seamlessly natively across every API target globally!

The **Auth Service** attaches a `company_id` specifically scoped within generated JWT limits via Oauth2 bounds. Every controller implicitly extracts `jwt.getClaimAsString("company_id")`, forcing it securely into database schema checks strictly prohibiting data bleeding globally. Additionally, all User tracking maps `user_id` scopes natively identically. No payload natively assumes isolation, it relies entirely upon the immutable token mapping parameters directly.

## Default Testing Credentials

Once Docker initializes, authenticate using placeholder credentials automatically mapped over isolated targets logically:

- **Username**: `admin@visaflow.com`
- **Password**: `password`
