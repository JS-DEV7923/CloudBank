# CloudBank Engineering Requirements

Source of truth: `PRD.md`

## PRD Validation Summary

The PRD is complete enough for implementation planning. It defines users, MVP scope, API surface, core data model, acceptance criteria, risks, and deployment intent.

### Blocking Ambiguities

None for MVP planning. The PRD includes open questions, but the recommended MVP decision resolves enough behavior to start implementation with one default currency, user-owned accounts, JWT authentication, required idempotency keys, PostgreSQL persistence, and integration tests.

### Non-Blocking Ambiguities

| ID | Ambiguity | Engineering Impact | Proposed MVP Handling |
| --- | --- | --- | --- |
| AMB-001 | Default currency value is not explicitly fixed. | Affects account and transaction defaults, tests, and seed data. | Use `USD` as the default currency unless changed before implementation. |
| AMB-002 | Account number format is not specified. | Affects account generation and validation. | Generate unique numeric account numbers plus UUID primary keys. |
| AMB-003 | Transfer behavior between same-owner accounts is not specified. | Affects authorization and transaction descriptions. | Use the same transfer workflow for all valid destination accounts. |
| AMB-004 | Idempotency key retention period is not specified. | Affects cleanup jobs and storage growth. | Do not expire idempotency records in MVP. |
| AMB-005 | Postman collection is optional. | Affects documentation deliverables. | Defer unless specifically requested. |

## Requirement Traceability

| ID | Requirement | Source PRD Section | Priority | Testable Behavior |
| --- | --- | --- | --- | --- |
| REQ-001 | Implement user registration with unique email and required profile fields. | 9.1, 16 | Must | Duplicate email returns `409`; valid user is persisted. |
| REQ-002 | Store passwords using secure one-way hashing. | 9.1, 12 | Must | Stored value is not plaintext and login validates through password encoder. |
| REQ-003 | Implement login that issues signed JWT access tokens. | 9.1, 10, 16 | Must | Valid credentials return token; invalid credentials return `401`. |
| REQ-004 | Protect all non-auth endpoints with JWT authentication. | 9.1, 10, 12 | Must | Missing or invalid token returns `401`. |
| REQ-005 | Enforce account ownership for account and transaction resources. | 8, 9.2, 12, 16 | Must | Access to another user's account returns `403` or `404` per final API convention. |
| REQ-006 | Allow authenticated users to create accounts. | 9.2, 10, 16 | Must | `POST /api/v1/accounts` creates account with zero balance and active status. |
| REQ-007 | Allow authenticated users to list and retrieve their accounts. | 9.2, 10, 16 | Must | User only receives accounts they own. |
| REQ-008 | Prevent direct balance mutation outside transaction workflows. | 9.2, 12 | Must | No public endpoint directly sets balance. |
| REQ-009 | Implement deposits as atomic balance and transaction updates. | 9.3, 16 | Must | Positive deposit increases balance and writes transaction. |
| REQ-010 | Implement withdrawals with insufficient-funds protection. | 9.4, 13, 16 | Must | Valid withdrawal decreases balance; insufficient funds returns `422`. |
| REQ-011 | Implement transfers as atomic debit, credit, and transaction records. | 9.5, 16 | Must | Source debited and destination credited in one database transaction. |
| REQ-012 | Reject invalid money movement amounts. | 9.3, 9.4, 9.5, 13 | Must | Zero or negative amount returns `400` validation error. |
| REQ-013 | Implement transaction history sorted by timestamp. | 9.6, 10, 16 | Must | History returns only authorized account activity ordered by timestamp. |
| REQ-014 | Support pagination for transaction history. | 9.6, 12 | Should | Request accepts page and size parameters with bounded size. |
| REQ-015 | Require idempotency keys for money movement endpoints. | 7, 9.7, 16 | Must | Missing key returns `400`; duplicate key returns original result. |
| REQ-016 | Detect conflicting idempotency key reuse. | 9.7, 13, 16 | Must | Same key with different request hash returns `409`. |
| REQ-017 | Persist idempotency response metadata. | 9.7 | Must | Record stores key, endpoint, request hash, status, response, and user. |
| REQ-018 | Use Bean Validation for request payloads. | 7, 12, 13 | Must | Invalid fields return structured `400` with field errors. |
| REQ-019 | Provide centralized exception handling. | 7, 13 | Must | API returns consistent error shape across controllers. |
| REQ-020 | Publish OpenAPI documentation. | 7, 9.8, 16 | Must | Swagger UI or OpenAPI JSON lists all public endpoints and schemas. |
| REQ-021 | Persist application data in PostgreSQL. | 7, 11, 15 | Must | Entities are mapped to PostgreSQL-compatible schema. |
| REQ-022 | Provide Docker Compose local setup. | 7, 15 | Must | Developer can run API and database locally with documented commands. |
| REQ-023 | Provide integration tests for core flows. | 6, 14, 16 | Must | CI executes integration tests covering auth, accounts, money movement, idempotency. |
| REQ-024 | Provide GitHub Actions CI for build and tests. | 4, 7, 14, 16 | Must | Pull requests and main pushes run build and tests. |
| REQ-025 | Provide Terraform-scoped AWS deployment infrastructure. | 4, 7, 15, 16 | Should | Terraform defines VPC, ECS Fargate, RDS PostgreSQL, CloudWatch logs, S3 artifacts, env vars, security groups. |
| REQ-026 | Log operational errors without leaking secrets. | 12 | Must | Logs include useful context and exclude passwords, JWTs, and secret values. |
| REQ-027 | Include request correlation identifiers in logs. | 12 | Should | Each request has traceable correlation ID in application logs. |
| REQ-028 | Keep local standard API latency below 300 ms under normal development load. | 12 | Should | Basic CRUD integration checks meet target in local environment. |

## Out of Scope

- Real payment rails, ACH, cards, wires, KYC, AML, loans, fraud detection, frontend UI, multi-currency MVP, admin dashboard, refresh tokens, password reset, event-driven ledger replication, and production compliance controls.
