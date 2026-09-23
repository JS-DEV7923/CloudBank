# CloudBank Component Design

Source inputs: `engineering-spec.md`, `requirements.md`, `technical-constraints.md`.

## 1. API Controllers

Responsibilities:

- Define `/api/v1` REST endpoints.
- Accept and validate request DTOs.
- Require authentication where applicable.
- Delegate business behavior to services.
- Return response DTOs without exposing entities directly.

Owned data: none.

Dependencies:

- Auth Service.
- Account Service.
- Money Movement Service.
- Transaction History Service.
- Idempotency Service through money movement services.

Failure modes:

- Invalid input returns `400`.
- Missing authentication returns `401`.
- Service exceptions are mapped by the global exception handler.

## 2. Security Layer

Responsibilities:

- Configure Spring Security.
- Validate JWTs.
- Resolve authenticated user identity.
- Define public and protected endpoint rules.
- Provide password encoder.

Owned data:

- JWT signing configuration is read from environment.

Dependencies:

- JWT Service.
- User Repository or User Details Service.

Failure modes:

- Missing, malformed, invalid, or expired token returns `401`.
- Misconfigured signing secret should fail application startup.

## 3. Auth Service

Responsibilities:

- Register users.
- Hash passwords.
- Authenticate credentials.
- Issue JWT access tokens through JWT Service.
- Enforce unique email behavior.

Owned data:

- User records through User Repository.

Dependencies:

- User Repository.
- Password Encoder.
- JWT Service.

Failure modes:

- Duplicate email returns `409`.
- Invalid login returns `401`.

## 4. Account Service

Responsibilities:

- Create accounts for authenticated users.
- Generate unique account numbers.
- Retrieve user-owned accounts.
- Enforce account ownership checks.

Owned data:

- Account records through Account Repository.

Dependencies:

- Account Repository.
- Authenticated user context.

Failure modes:

- Account not found returns `404`.
- Cross-user access returns `404` by recommended architecture convention.
- Account number collision retries generation or returns controlled server error if exhausted.

## 5. Money Movement Service

Responsibilities:

- Execute deposits, withdrawals, and transfers.
- Validate positive amounts and supported currency.
- Enforce source account ownership.
- Check sufficient funds.
- Lock account rows for safe balance updates.
- Create transaction and transfer records.
- Coordinate idempotency persistence and replay.

Owned data:

- Account balances.
- Transaction records.
- Transfer records.

Dependencies:

- Account Repository.
- Transaction Repository.
- Transfer Repository.
- Idempotency Service.

Scaling and reliability:

- Stateless service methods can scale horizontally.
- PostgreSQL row locks serialize concurrent updates to the same account.

Failure modes:

- Insufficient funds returns `422`.
- Invalid amount returns `400`.
- Idempotency conflict returns `409`.
- Database transaction failure rolls back balance and audit changes.

## 6. Idempotency Service

Responsibilities:

- Validate required idempotency keys for money movement endpoints.
- Compute canonical request hashes.
- Detect replay vs conflict.
- Store response status and body for replay.

Owned data:

- Idempotency records.

Dependencies:

- Idempotency Repository.
- Serialization utility for canonical request hashing.

Scaling and reliability:

- Unique database constraint protects against concurrent duplicate first requests.
- Service must handle race between two identical first requests with the same key.

Failure modes:

- Missing key returns `400`.
- Key reused with different request hash returns `409`.
- Duplicate insert race should reload existing record and replay or conflict.

## 7. Transaction History Service

Responsibilities:

- Validate account ownership.
- Return paginated transaction history.
- Apply stable timestamp sorting.

Owned data:

- Reads transaction records, does not own money movement writes.

Dependencies:

- Account Repository.
- Transaction Repository.

Failure modes:

- Cross-user history access returns `404` by recommended convention.
- Invalid pagination returns `400`.

## 8. Persistence Layer

Responsibilities:

- Persist domain entities through Spring Data JPA.
- Define locking queries for money movement.
- Expose access patterns needed by services.

Owned data:

- PostgreSQL schema and entity mappings.

Dependencies:

- PostgreSQL.

Failure modes:

- Constraint violations are translated to application exceptions.
- Lock timeouts should return controlled `409` or `503` if configured.

## 9. Exception Handling

Responsibilities:

- Convert validation, security, domain, and persistence exceptions into stable error responses.
- Include correlation ID where available.
- Avoid leaking stack traces or secrets.

Owned data: none.

Dependencies:

- Request context for path and correlation ID.

Failure modes:

- Unhandled exceptions return generic `500` with safe message.

## 10. Observability Components

Responsibilities:

- Generate or propagate correlation IDs.
- Log request method, path, status, duration, and correlation ID.
- Emit safe diagnostics for auth and money movement failures.
- Optionally expose actuator health and metrics.

Owned data:

- Application logs and runtime metrics.

Dependencies:

- Logging framework.
- CloudWatch in AWS deployment.

Failure modes:

- Logging failure must not block API behavior.

## 11. OpenAPI Component

Responsibilities:

- Generate API documentation.
- Describe auth requirements, request/response schemas, status codes, and errors.

Owned data: none.

Dependencies:

- Controller annotations and DTO schemas.

Failure modes:

- Documentation drift is mitigated through generated OpenAPI and endpoint smoke tests.
