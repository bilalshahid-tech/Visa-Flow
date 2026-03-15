# 🚀 VisaFlow --- Smart Visa & Immigration Workflow Platform

<div align="center">

<strong>Production-grade B2B SaaS platform for visa consultancy firms</strong><br>
Automates client management, document processing, and visa risk analysis
using a <strong>scalable microservices architecture</strong>.

<br><br>

<img src="https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen" alt="Spring Boot"/>
<img src="https://img.shields.io/badge/Apache%20Kafka-3.5-231F20" alt="Apache Kafka"/>
<img src="https://img.shields.io/badge/PostgreSQL-15-336791" alt="PostgreSQL"/>
<img src="https://img.shields.io/badge/Docker-24.0-2496ED" alt="Docker"/>
<img src="https://img.shields.io/badge/Architecture-Microservices-ff6b6b" alt="Architecture"/>
<img src="https://img.shields.io/badge/Security-JWT%20%7C%20OAuth2-black" alt="Security"/>
<img src="https://img.shields.io/badge/License-MIT-yellow" alt="License"/>

</div>

---

# 📌 Overview

**VisaFlow** is a **modern microservices-based SaaS platform** designed
for **visa consultancy firms** to manage immigration workflows
efficiently.

It combines:

- CRM capabilities
- Document management
- Workflow automation
- Risk analysis
- Event-driven architecture

The platform enables consultancies to **manage visa applications
end‑to‑end**, improve operational efficiency, and provide **real‑time
updates to clients**.

---

# ⚡ Key Highlights

### 🏢 Enterprise Ready

Multi‑tenant architecture allowing multiple consultancy firms to operate
on a single platform securely.

### ⚙️ Automated Workflow

Visa cases move through configurable stages such as:

Initial Review → Document Collection → Embassy Submission → Interview →
Decision

### 📊 Intelligent Risk Analysis

Rule‑based scoring system designed to evolve into **ML-powered visa
success prediction**.

### 🔔 Event‑Driven Communication

Kafka-based asynchronous messaging ensures **scalable and reliable
service communication**.

### 🔒 Enterprise Security

JWT authentication, OAuth2 integration, and role-based access control.

### 📁 Secure Document Handling

Document storage with S3/MinIO, version control, and verification
workflow.

---

# 🏗 System Architecture

VisaFlow follows a **distributed microservices architecture** with an
API Gateway and event-driven communication.

                            ┌───────────────────────────┐
                            │        API Gateway        │
                            │   Spring Cloud Gateway    │
                            └─────────────┬─────────────┘
                                          │
            ┌───────────────┬─────────────┼─────────────┬──────────────┐
            ▼               ▼             ▼             ▼              ▼

       Auth Service    User Service   Case Service  Document Service  Risk Service
       (JWT/OAuth2)    (MultiTenant)  (Workflow)    (S3/MinIO)        (Scoring)

            └───────────────┬───────────────┬───────────────┬──────────┘
                            ▼               ▼               ▼

                      Notification Service        Audit Service
                           (Kafka)                 (Compliance)

---

# 📡 Event Driven Flow

    Service Event → Kafka Topic → Consumer Services → Notification Delivery

Example:

    Case Submitted → Kafka → Notification Service → Email/SMS Sent

This architecture ensures:

- Loose coupling
- High scalability
- Reliable asynchronous communication

---

# ✨ Core Features

## 🔐 Authentication & Authorization

- JWT access & refresh tokens
- OAuth2 login (Google)
- Role-based access control
- Secure multi-tenant isolation

Roles:

- **Admin**
- **Consultant**
- **Client**

---

## 👥 User & Company Management

- Consultancy onboarding
- Subscription-based companies
- Consultant profile management
- Client profile management
- Team collaboration

---

## 📂 Case Management

Track visa applications through lifecycle stages:

1.  Initial Review
2.  Document Collection
3.  Embassy Submission
4.  Interview Preparation
5.  Final Decision

Capabilities:

- Consultant assignment
- Deadline tracking
- Automated reminders
- Case status updates

---

## 📎 Document Management

- Secure document uploads
- AWS S3 / MinIO storage
- Document verification workflow
- Version history
- Audit trail

---

## 🧠 Risk Analysis Engine

VisaFlow includes a **rule-based risk engine** that evaluates visa
approval probability.

Scoring parameters:

- Financial stability
- Travel history
- Previous visa refusals
- Profile completeness
- Document consistency

Outputs:

- Risk score
- Recommendations for consultants

Future roadmap: **Machine Learning prediction models**.

---

## 🔔 Notification System

Kafka-powered notification system supports:

- Email alerts
- SMS notifications
- Future push notifications

Events triggering notifications:

- Case updates
- Document requests
- Interview scheduling
- Visa decisions

---

## 📊 Audit & Compliance

- Complete activity logs
- Immutable audit trail
- IP tracking
- GDPR-ready architecture

Tracks:

- Who performed an action
- What action occurred
- When it occurred

---

# 🛠 Tech Stack

## Backend

Technology Purpose

---

Java 17 Core language
Spring Boot Application framework
Spring Security Authentication & authorization
Spring Cloud Gateway API gateway
Spring Data JPA ORM & persistence
Apache Kafka Event streaming
Resilience4j Circuit breakers

---

## Database & Storage

Technology Purpose

---

PostgreSQL Primary database
Flyway Schema migrations
AWS S3 / MinIO Document storage
Redis Caching (planned)

---

## DevOps

Technology Purpose

---

Docker Containerization
Docker Compose Local orchestration
GitHub Actions CI/CD
ELK Stack Centralized logging
Prometheus Metrics
Grafana Monitoring dashboards

---

# 📦 Microservices

Service Port Description

---

API Gateway 8080 Routing & security
Auth Service 8081 Authentication
User Service 8082 Users & companies
Case Service 8083 Visa workflows
Document Service 8084 File management
Notification Service 8085 Kafka consumers
Risk Service 8086 Risk scoring
Audit Service 8087 Compliance logging

---

# 🚀 Getting Started

## Prerequisites

- Java 17+
- Docker
- Docker Compose
- Maven
- PostgreSQL
- Apache Kafka

---

## Installation

### 1️⃣ Clone Repository

    git clone https://github.com/bilalshahid-tech/Visa-Flow.git
    cd Visa-Flow

### 2️⃣ Start Infrastructure

    docker-compose up -d postgres kafka

### 3️⃣ Build Services

    mvn clean package

### 4️⃣ Run Platform

    docker-compose up -d

---

## Access Services

Service URL

---

API Gateway http://localhost:8080
Auth Service http://localhost:8081
User Service http://localhost:8082

---

# ⚙️ Environment Variables

    DB_HOST=localhost
    DB_PORT=5432
    DB_NAME=visaflow
    DB_USERNAME=postgres
    DB_PASSWORD=postgres

    KAFKA_BOOTSTRAP_SERVERS=localhost:9092

    JWT_SECRET=your-secret-key
    JWT_EXPIRATION=86400000

    AWS_ACCESS_KEY=your-access-key
    AWS_SECRET_KEY=your-secret-key
    AWS_S3_BUCKET=visaflow-documents

---

# 📚 API Documentation

Swagger UI available when services run:

    http://localhost:8081/swagger-ui.html
    http://localhost:8082/swagger-ui.html
    http://localhost:8083/swagger-ui.html

---

# 🐳 Docker Deployment

Build and run the full stack:

    docker-compose build
    docker-compose up -d

Scale services:

    docker-compose up -d --scale notification-service=3

View logs:

    docker-compose logs -f

---

# 📈 Observability

Monitoring endpoints:

- `/actuator/health`
- `/actuator/prometheus`

Tools used:

- Prometheus
- Grafana
- ELK Stack

Future:

- OpenTelemetry tracing

---

# 🗺 Development Roadmap

### Phase 1 --- MVP ✅

- Auth service
- API gateway
- Basic case workflow
- Kafka integration

### Phase 2 --- Core Features 🚧

- Document service
- Risk engine
- Audit service
- Multi-tenancy

### Phase 3 --- Production Ready

- CI/CD automation
- Monitoring stack
- Rate limiting
- Fault tolerance

---

# 🤝 Contributing

1.  Fork repository
2.  Create feature branch

```

```

    git checkout -b feature/amazing-feature

3.  Commit changes

```

```

    git commit -m "Add amazing feature"

4.  Push changes

```

```

    git push origin feature/amazing-feature

5.  Open Pull Request

---

# 👨‍💻 Author

**Bilal Shahid**

GitHub: https://github.com/bilalshahid-tech

---

# ⭐ Support

If you find this project useful:

⭐ Star the repository\
🍴 Fork it\
🚀 Share it with others

---

<div align="center">
  <strong>VisaFlow — Modernizing Immigration Workflow Platforms</strong>
</div>
