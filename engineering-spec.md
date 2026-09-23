# CloudBank Engineering Specification

Source of truth: `PRD.md`

## 1. System Context

CloudBank is a Spring Boot REST API backed by PostgreSQL. It provides authenticated user and account workflows plus transactional money movement. The system is a portfolio-grade backend and must model production concerns without integrating with real banking networks.

## 2. Architecture Overview

Use a layered Spring Boot architecture:

- `controller`: REST endpoints, request validation entry points, response mapping.
- `service`: business rules, transactions, ownership checks, idempotency orchestration.
- `repository`: Spring Data JPA persistence access.
- `domain` or `entity`: JPA entities and enums.
- `dto`: request and response contracts.
- `security`: JWT filters, authentication provider configuration, password encoder.
- `exception`: custom exceptions and global exception handler.
- `config`: OpenAPI, application config, CORS if required, data source config.
- `observability`: request correlation filter and structured logging helpers.

## 3. API Behavior

### Authentication

`POST /api/v1/auth/register`

- Accepts user identity and credentials.
- Validates required fields and email format.
- Hashes password before persistence.
- Enforces unique email.
- Returns created user DTO without password hash.

`POST /api/v1/auth/login`

- Accepts email and password.
- Verifies credentials using password encoder.
- Returns JWT access token and basic user identity metadata.

### Account Management

`POST /api/v1/accounts`

- Requires JWT.
- Creates active account for authenticated user.
- Uses default MVP currency.
- Sets starting balance to zero.
- Generates unique account number.

`GET /api/v1/accounts`

- Requires JWT.
- Returns accounts owned by authenticated user only.

`GET /api/v1/accounts/{accountId}`

- Requires JWT.
- Returns account if owned by authenticated user.
- Rejects or hides accounts owned by others.

### Deposits

`POST /api/v1/accounts/{accountId}/deposits`

- Requires JWT and idempotency key.
- Validates ownership and positive amount.
- Uses a database transaction.
- Locks or version-checks account balance before update.
- Creates transaction record and idempotency record.
- Returns updated account balance and transaction reference.

### Withdrawals

`POST /api/v1/accounts/{accountId}/withdrawals`

- Requires JWT and idempotency key.
- Validates ownership, positive amount, and sufficient funds.
- Uses a database transaction.
- Creates transaction record and idempotency record.
- Returns updated account balance and transaction reference.

### Transfers

`POST /api/v1/transfers`

- Requires JWT and idempotency key.
- Validates source ownership, destination existence, positive amount, and sufficient funds.
- Debits source and credits destination inside one database transaction.
- Creates transfer record and transaction records for source and destination.
- Returns transfer reference and resulting source balance.

### Transaction History

`GET /api/v1/accounts/{accountId}/transactions`

- Requires JWT.
- Validates account ownership.
- Returns paginated transactions sorted by timestamp descending by default.

## 4. Data Model

### User

Fields:

- `id`: UUID or database-generated primary key.
- `email`: unique, required.
- `passwordHash`: required, never returned by API.
- `firstName`: required.
- `lastName`: required.
- `createdAt`, `updatedAt`: server-managed timestamps.

Indexes:

- Unique index on `email`.

### Account

Fields:

- `id`: UUID or database-generated primary key.
- `userId`: owner reference.
- `accountNumber`: unique generated value.
- `balance`: decimal value, use `BigDecimal`.
- `currency`: default MVP value, proposed `USD`.
- `status`: proposed enum `ACTIVE`, `CLOSED`, `FROZEN`.
- `createdAt`, `updatedAt`.

Indexes:

- Unique index on `accountNumber`.
- Index on `userId`.

Concurrency:

- Use row-level locking for money movement queries or optimistic locking with a version column.

### Transaction

Fields:

- `id`.
- `accountId`.
- `type`: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_DEBIT`, `TRANSFER_CREDIT`.
- `amount`: positive `BigDecimal`.
- `currency`.
- `balanceAfter`.
- `relatedAccountId`.
- `transferId`.
- `description`.
- `createdAt`.

Indexes:

- Composite index on `accountId`, `createdAt`.

### Transfer

Fields:

- `id`.
- `sourceAccountId`.
- `destinationAccountId`.
- `amount`.
- `currency`.
- `status`: `COMPLETED`, `FAILED` if persisted failures are later required.
- `createdAt`.

### IdempotencyRecord

Fields:

- `id`.
- `userId`.
- `idempotencyKey`.
- `endpoint`.
- `requestHash`.
- `responseStatus`.
- `responseBody`.
- `createdAt`.

Indexes:

- Unique index on `userId`, `endpoint`, `idempotencyKey`.

## 5. Idempotency Flow

1. Controller receives money movement request with idempotency key header.
2. Service computes canonical request hash from authenticated user, endpoint, path parameters, and request body.
3. Service checks for existing idempotency record by user, endpoint, and key.
4. If record exists and hash matches, return stored response without executing money movement.
5. If record exists and hash differs, return `409 Conflict`.
6. If no record exists, execute business operation inside transaction.
7. Persist idempotency record with request hash and response metadata.
8. Return new operation response.

Recommended header: `Idempotency-Key`.

## 6. Error Handling

Use a global exception handler to map exceptions to a common response:

- `status`: HTTP status.
- `code`: stable application error code.
- `message`: human-readable summary.
- `fieldErrors`: optional list of field validation errors.
- `path`: request path.
- `timestamp`: server timestamp.
- `correlationId`: included when available.

Representative mappings:

- Validation failure: `400`.
- Missing or invalid JWT: `401`.
- Ownership violation: `403` or `404`.
- Missing resource: `404`.
- Duplicate email: `409`.
- Idempotency conflict: `409`.
- Insufficient funds: `422`.

## 7. Transaction and Consistency Rules

- Deposits, withdrawals, and transfers must use service-layer `@Transactional` boundaries.
- Balance updates and transaction inserts must commit or roll back together.
- Transfer debit and credit must commit or roll back together.
- Balance arithmetic must use `BigDecimal`, never floating point.
- Amounts must be positive and normalized to the supported currency scale.
- Direct external balance-setting endpoints are not allowed.

## 8. Testing Strategy

Required test layers:

- Unit tests for service-layer business rules.
- Integration tests for REST controllers and persistence.
- Authentication tests for protected endpoints.
- Idempotency tests for duplicate and conflicting retries.
- Concurrency-oriented test for double withdrawal or transfer attempts when feasible.

Recommended tools:

- JUnit 5.
- Spring Boot Test.
- Testcontainers PostgreSQL or a dedicated test PostgreSQL service.
- MockMvc or RestAssured.

## 9. Infrastructure and CI

### Local

- Docker Compose runs PostgreSQL and optionally the API container.
- `.env.example` documents required variables.

### CI

- GitHub Actions checks out code, sets up Java, runs build and tests, and publishes test reports if configured.

### AWS Scope

Terraform modules or directories define:

- VPC networking.
- ECS Fargate cluster, service, and task definition.
- RDS PostgreSQL instance.
- CloudWatch log groups.
- S3 artifact bucket.
- Environment variables.
- Security groups allowing API-to-database traffic only.

## 10. Areas Not Specified in PRD

- Frontend UI: out of scope.
- Real bank/payment integrations: out of scope.
- Production secrets manager: deferred.
- Refresh tokens and password reset: deferred.
- Rate limiting: deferred.
- Account closure rules: not specified.
- Idempotency expiration and cleanup: not specified for MVP.
