# CloudBank Security Architecture

Source input: `security-requirements.md`.

## 1. Security Model

CloudBank uses stateless JWT authentication and service-layer authorization. Authentication proves user identity; authorization is checked on every account-scoped operation.

## 2. Trust Boundaries

| Boundary | Risk | Control |
| --- | --- | --- |
| Client to API | Untrusted input, missing auth, malicious payloads | TLS in deployed environments, JWT validation, Bean Validation, bounded request sizes. |
| API controller to service | Authenticated user may request unauthorized resources | Service-layer ownership checks. |
| API to PostgreSQL | Credential leakage, broad database access | Environment secrets, private network, restricted security groups. |
| Logs and errors | Secret leakage | Safe error responses, secret redaction, no raw JWT/password logging. |

## 3. Authentication

- Registration and login are public.
- Protected endpoints require `Authorization: Bearer <JWT>`.
- JWT claims must include stable user ID.
- JWT signing secret is environment-specific.
- Missing, invalid, malformed, or expired tokens return `401`.

## 4. Password Handling

- Passwords are accepted only at registration and login.
- Passwords are hashed with a secure password encoder.
- Password hashes are never returned.
- Passwords and hashes are never logged.

## 5. Authorization

Rules:

- Users may create accounts only for themselves.
- Users may list only their accounts.
- Users may retrieve only their accounts.
- Users may deposit to and withdraw from only their accounts.
- Users may transfer from only their accounts.
- Users may view transaction history only for their accounts.

Recommended resource disclosure policy:

- Return `404` for non-owned account IDs to avoid confirming resource existence.
- Keep this convention consistent across account, transaction history, deposit, withdrawal, and transfer source checks.

## 6. Input Validation

- Request DTOs use Bean Validation.
- Email fields validate format and size.
- Password fields validate minimum complexity if implemented.
- Money amounts must be positive and use decimal scale.
- Description fields must have maximum length.
- Pagination parameters must be bounded.

## 7. Idempotency Security

- Idempotency keys are required for money movement endpoints.
- Key scope includes authenticated user and endpoint.
- Request hash includes path parameters and request body.
- Conflicting reuse returns `409`.
- Idempotency response replay must not expose another user's operation because records are scoped to user.

## 8. Data Protection

- Secrets are environment variables.
- No real secrets in repository.
- RDS is private in AWS scope.
- S3 artifact bucket blocks public access by default.
- CloudWatch logs must not include JWTs, passwords, or database credentials.

## 9. Threat Considerations

| Threat | Control |
| --- | --- |
| Credential stuffing | Return generic login failures; rate limiting deferred but should be easy to add. |
| Horizontal privilege escalation | Centralized ownership checks and integration tests. |
| Duplicate money movement | Required idempotency keys and database uniqueness. |
| Race condition balance abuse | Row-level locking and database transactions. |
| Sensitive error leakage | Global exception handler with safe messages. |
| Token forgery | Strong signing secret and signature validation. |

## 10. Compliance Position

CloudBank is a portfolio project. It does not provide regulated financial services and does not implement KYC, AML, PCI, or real payment processing.
