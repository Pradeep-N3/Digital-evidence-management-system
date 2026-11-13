# Digital Evidence & Case Management System (DECMS)

[![Forensic Integrity](https://img.shields.io/badge/Forensic%20Integrity-SHA--256%20Verified-06b6d4)](https://github.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20OOP%20%7C%20Layered%20MVC-3b82f6)](https://github.com)
[![Standard](https://img.shields.io/badge/Compliance-ISO%2FIEC%2027037-10b981)](https://github.com)

A web-based platform tailored for digital forensic investigators, cybercrime units, and compliance auditors. It guarantees **technical evidentiary integrity**, **unbroken chain of custody**, and **tamper-evident audit trails** throughout the entire lifecycle of digital evidence.

---

## 🛡️ Core Capabilities

- **Case Lifecycle Management**: End-to-end case tracking (`OPEN` $\to$ `UNDER_INVESTIGATION` $\to$ `PENDING_REVIEW` $\to$ `CLOSED`) with auto-generated identifiers (`CASE-YYYY-XXXX`).
- **Digital Evidence Vault**: Ingestion and categorization for images, documents, videos, audio, and system logs with deep metadata extraction.
- **Cryptographic Integrity Verification Engine**: Automatic SHA-256 baseline hashing on ingest and live on-demand byte-level re-computation against physical disk storage.
- **Tamper-Evident Hash-Chained Custody Ledger**: Every custody action (`UPLOADED`, `TRANSFERRED`, `VERIFIED`, `DOWNLOADED`) is recorded in an immutable, Merkle-style block where $\text{RecordHash} = \text{SHA-256}(\text{PrevHash} + \text{EvidenceID} + \text{Action} + \dots)$.
- **Court-Ready PDF Dossier Export**: One-click generation of digitally stamped forensic reports containing case specifications, evidence registers, complete custody timelines, and legal integrity attestations.
- **Granular Role-Based Access Control (RBAC)**: Enforced via Spring Security with JWT for three forensic personas:
  - `ROLE_ADMIN`: Manages users, configures policies, views security audit logs.
  - `ROLE_INVESTIGATOR`: Opens cases, uploads evidence, requests verifications, and transfers custody.
  - `ROLE_AUDITOR`: Read-only oversight, inspects chain of custody, validates ledger integrity.

---

## 🧱 Clean Object-Oriented Architecture (OOP)

| OOP Paradigm | Concrete DECMS Implementation |
| :--- | :--- |
| **Encapsulation** | Strict private fields with defensive copies and business rule validations in domain entities. |
| **Inheritance** | Polymorphic user hierarchy (`Administrator`, `Investigator`, `Auditor` $\to$ `User`) and polymorphic evidence hierarchy (`ImageEvidence`, `DocumentEvidence`, `MediaEvidence`, `LogEvidence` $\to$ `Evidence`). |
| **Interfaces & Polymorphism** | Polymorphic behavior standardized through `Verifiable`, `Auditable`, and `Exportable` interfaces. |
| **Composition** | `Case` owns a collection of `Evidence` entities; `Evidence` owns an ordered history of `CustodyRecord` blocks. |
| **Cryptographic Hashing** | Stream-based `java.security.MessageDigest` for zero-memory-leak hashing of multi-gigabyte files. |

---

## 🚀 Quick Start Guide

### 1. Default Pre-Configured Forensic Accounts

The system seeds three operational accounts upon first initialization:

| Forensic Role | Username | Password | Operational Purpose |
| :--- | :--- | :--- | :--- |
| **Forensic Administrator** | `admin` | `admin123` | System oversight, user management, audit review |
| **Lead Investigator** | `investigator` | `investigator123` | Case intake, evidence upload, custody transfers |
| **Judicial Auditor** | `auditor` | `auditor123` | Independent oversight, ledger validation, PDF export |

*(Quick-login buttons are also provided on the login page for rapid 1-click evaluation).*

---

### 2. Running the Spring Boot Backend

```bash
cd backend
mvn spring-boot:run
```
- Backend runs on `http://localhost:8080`.
- By default, uses an embedded **H2 file-backed database** (`./decms-db`) that requires zero external setup.
- H2 Console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./decms-db`).
- **Production MySQL Switch**: Change `spring.profiles.active: prod` in `application.yml` to switch to MySQL.

---

### 3. Running the React Frontend

```bash
cd frontend
npm run dev
```
- Frontend starts at `http://localhost:5173`.
- Automatically proxies all `/api` requests to backend at `http://localhost:8080`.

---

## 📡 REST API Summary

- `POST /api/auth/login` — Authenticate and receive JWT token
- `GET /api/cases` — List all investigation cases
- `POST /api/cases` — Open new investigation case
- `GET /api/cases/{caseId}` — Get case profile and attached evidence
- `PATCH /api/cases/{caseId}/status` — Transition case status
- `GET /api/evidence` — Browse central digital evidence vault
- `POST /api/cases/{caseId}/evidence` — Upload evidence file (multipart)
- `GET /api/evidence/{evidenceId}` — Get evidence details and baseline SHA-256
- `GET /api/evidence/{evidenceId}/download` — Stream download physical artifact
- `POST /api/evidence/{evidenceId}/verify` — Recompute SHA-256 and verify file integrity
- `GET /api/evidence/{evidenceId}/custody` — Retrieve chronological custody blocks
- `POST /api/evidence/{evidenceId}/transfer` — Append transfer record to custody ledger
- `GET /api/reports/case/{caseId}/pdf` — Stream down court-ready forensic PDF report
- `GET /api/dashboard/stats` — Metrics, integrity health meter, and activity feed
- `GET /api/audit/logs` — Comprehensive security and administrative logs
- `GET /api/audit/validate-all` — Cryptographically walk and validate all custody chains
