# CloudBank Acceptance Criteria

Source of truth: `PRD.md`

## Authentication

### AC-001: Register User

- Given a valid registration request with unique email, when `POST /api/v1/auth/register` is called, then the API returns `201 Created` and a user representation without password fields.
- Given an email that already exists, when registration is attempted, then the API returns `409 Conflict`.
- Given invalid fields, when registration is attempted, then the API returns `400 Bad Request` with field-level validation errors.

### AC-002: Login

- Given valid credentials, when `POST /api/v1/auth/login` is called, then the API returns `200 OK` with a signed JWT access token.
- Given invalid credentials, when login is attempted, then the API returns `401 Unauthorized`.

### AC-003: Protected Endpoints

- Given no JWT, when a protected endpoint is called, then the API returns `401 Unauthorized`.
- Given an invalid or expired JWT, when a protected endpoint is called, then the API returns `401 Unauthorized`.
- Given a valid JWT, when a protected endpoint is called, then the request is processed in the context of the authenticated user.

## Accounts

### AC-004: Create Account

- Given an authenticated user, when `POST /api/v1/accounts` is called, then the API creates an active account owned by that user.
- The new account has zero opening balance unless a future approved requirement allows otherwise.
- The new account has a unique account number and persistent account ID.

### AC-005: List and Retrieve Accounts

- Given an authenticated user with accounts, when `GET /api/v1/accounts` is called, then only that user's accounts are returned.
- Given an authenticated user and an owned account ID, when `GET /api/v1/accounts/{accountId}` is called, then account details are returned.
- Given an authenticated user and another user's account ID, when account retrieval is attempted, then the API returns `403 Forbidden` or `404 Not Found` according to the final error disclosure convention.

## Deposits

### AC-006: Deposit Funds

- Given an authenticated user, an owned account, a positive amount, and an idempotency key, when `POST /api/v1/accounts/{accountId}/deposits` is called, then the account balance increases by that amount.
- A deposit transaction is created with type `DEPOSIT`, amount, currency, balance after, and timestamp.
- The balance update and transaction insert occur in one database transaction.
- Given zero or negative amount, the API returns `400 Bad Request`.

## Withdrawals

### AC-007: Withdraw Funds

- Given sufficient funds, a positive amount, and an idempotency key, when `POST /api/v1/accounts/{accountId}/withdrawals` is called, then the account balance decreases by that amount.
- A withdrawal transaction is created with type `WITHDRAWAL`, amount, currency, balance after, and timestamp.
- Given insufficient funds, the API returns `422 Unprocessable Entity` and does not change balance.
- Given zero or negative amount, the API returns `400 Bad Request`.

## Transfers

### AC-008: Transfer Funds

- Given an authenticated user, an owned source account, a valid destination account, sufficient funds, a positive amount, and an idempotency key, when `POST /api/v1/transfers` is called, then the source account is debited and destination account is credited.
- Debit, credit, transfer record, and transaction records are persisted atomically.
- Given a source account not owned by the user, the API returns `403 Forbidden` or `404 Not Found`.
- Given insufficient source funds, the API returns `422 Unprocessable Entity` and neither account balance changes.
- Given an invalid destination account, the API returns `404 Not Found`.

## Transaction History

### AC-009: Account Transaction History

- Given an authenticated user and an owned account, when `GET /api/v1/accounts/{accountId}/transactions` is called, then account transaction history is returned.
- Results are sorted by transaction timestamp in descending order by default unless the API contract specifies otherwise.
- Results support pagination with page and size query parameters.
- Given another user's account ID, the API returns `403 Forbidden` or `404 Not Found`.

## Idempotency

### AC-010: Duplicate Retry

- Given a completed money movement request with idempotency key `K`, when the same user repeats the same endpoint and request payload with key `K`, then the API returns the original response and does not create additional balance changes or transactions.

### AC-011: Conflicting Retry

- Given a completed money movement request with idempotency key `K`, when the same user repeats the same endpoint with a materially different payload and key `K`, then the API returns `409 Conflict`.

### AC-012: Missing Idempotency Key

- Given a money movement endpoint request without an idempotency key, when the API receives the request, then it returns `400 Bad Request`.

## API Error Handling and Documentation

### AC-013: Error Shape

- All handled API errors return status code, stable error code, human-readable message, request path, timestamp, and field errors when applicable.

### AC-014: OpenAPI

- OpenAPI documentation includes all public endpoints, request bodies, response bodies, status codes, authentication requirements, and representative error responses.

## Testing, CI, and Deployment Scope

### AC-015: Integration Tests

- Integration tests cover registration, login, account creation, deposit, withdrawal, transfer, transaction history, duplicate idempotency retry, and conflicting idempotency retry.

### AC-016: CI

- GitHub Actions runs build and tests for pull requests and pushes to the main branch.

### AC-017: Local Run

- A developer can run PostgreSQL and the API locally with Docker Compose using documented environment variables.

### AC-018: Terraform Scope

- Terraform configuration defines VPC, ECS Fargate service, RDS PostgreSQL, CloudWatch logs, S3 artifacts, environment variables, and security groups restricting database access to the API service.
