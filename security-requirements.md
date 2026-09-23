# CloudBank Security Requirements

Source of truth: `PRD.md`

## Authentication

- SEC-001: Public endpoints are limited to registration, login, OpenAPI docs if enabled, and health checks if added.
- SEC-002: Protected endpoints require valid JWT authentication.
- SEC-003: JWTs must be signed with an environment-specific secret.
- SEC-004: Invalid, missing, malformed, or expired JWTs must return `401 Unauthorized`.
- SEC-005: JWT claims must identify the authenticated user in a stable way, such as user ID.

## Password Security

- SEC-006: Passwords must never be stored or logged in plaintext.
- SEC-007: Passwords must be hashed with a secure password encoder.
- SEC-008: Login must compare passwords through the encoder verification API, not manual hash comparison.
- SEC-009: API responses must never include `passwordHash`.

## Authorization

- SEC-010: Users may create accounts only for themselves.
- SEC-011: Users may list and retrieve only their own accounts.
- SEC-012: Users may deposit to, withdraw from, and view transaction history only for accounts they own.
- SEC-013: Users may transfer only from source accounts they own.
- SEC-014: Destination accounts may be credited when valid, but destination owner data must not be exposed beyond what the API contract allows.

## Input Validation and Abuse Prevention

- SEC-015: Request DTOs must use Bean Validation for required fields, email format, amount positivity, and size limits.
- SEC-016: Money movement amount must reject zero and negative values.
- SEC-017: Request bodies should have reasonable maximum sizes.
- SEC-018: Rate limiting is deferred, but the code should not assume unlimited safe request volume.

## Idempotency and Auditability

- SEC-019: Money movement endpoints must require idempotency keys.
- SEC-020: Idempotency records must bind key usage to authenticated user, endpoint, and request hash.
- SEC-021: Reuse of a key with a different request hash must return `409 Conflict`.
- SEC-022: Transaction records must preserve enough detail to audit deposits, withdrawals, and transfers.

## Data Protection

- SEC-023: Sensitive configuration must come from environment variables or secure runtime configuration.
- SEC-024: Repository files must not contain real secrets.
- SEC-025: Logs must not include passwords, JWTs, database passwords, or signing secrets.
- SEC-026: Error responses must not expose stack traces or internal implementation details.

## Infrastructure Security

- SEC-027: AWS RDS security group must restrict database access to the ECS service security group.
- SEC-028: Public inbound access to the database is not allowed.
- SEC-029: CloudWatch logs must avoid secret leakage.
- SEC-030: S3 artifact bucket should block public access unless a future requirement explicitly changes this.

## Compliance Position

- SEC-031: CloudBank is a portfolio project and must not claim real banking compliance.
- SEC-032: KYC, AML, fraud detection, and regulated payment processing are out of scope.

## Security Acceptance Checks

- Protected endpoint without token returns `401`.
- User cannot retrieve another user's account.
- User cannot withdraw or transfer from another user's account.
- Duplicate idempotency key with different payload returns `409`.
- API responses do not expose password hashes or secrets.
- Logs do not include raw JWT or password values.
