# CloudBank Technical Constraints

Source of truth: `PRD.md`

## Platform Constraints

- Backend language: Java.
- Backend framework: Spring Boot.
- API style: REST.
- Database: PostgreSQL.
- Authentication: Spring Security with JWT.
- Containerization: Docker and Docker Compose.
- CI: GitHub Actions.
- Cloud scope: AWS ECS Fargate, RDS PostgreSQL, CloudWatch, S3, and Terraform.

## Data Constraints

- Monetary values must use decimal-safe representation, such as Java `BigDecimal`.
- Balance changes must be persisted transactionally.
- Account balances cannot be changed directly by public API calls.
- Users can access only their own accounts and account transaction history.
- Idempotency records must be unique per user, endpoint, and idempotency key.
- Password hashes must be stored; plaintext passwords must never be stored.

## API Constraints

- Versioned endpoint prefix: `/api/v1`.
- Protected endpoints require JWT authentication.
- Money movement endpoints require an idempotency key.
- Request payloads must use Bean Validation.
- Errors must follow a consistent machine-readable shape.
- OpenAPI documentation must cover endpoints, schemas, status codes, auth, and errors.

## Reliability Constraints

- Deposits, withdrawals, and transfers must run within database transactions.
- Transfer debit and credit must be atomic.
- Duplicate client retries must not duplicate transactions.
- Conflicting idempotency key reuse must be rejected.
- Balance updates must protect against concurrent modification through row-level locking or optimistic locking.

## Performance Constraints

- Standard local CRUD responses should complete within 300 ms under normal development load.
- Transaction history must support pagination.
- API responses must avoid unbounded transaction lists.

## Security Constraints

- JWT signing secret must come from environment-specific configuration.
- Sensitive values must not be logged.
- API responses must not expose password hash, token secrets, database credentials, or internal stack traces.
- Database access in AWS scope must be restricted to the API service security group.

## Operational Constraints

- Local development must be runnable through Docker Compose.
- Repository documentation must allow a new developer to run locally in under 10 minutes.
- CI must run build and tests on pull requests and pushes to main.
- Terraform should be scoped for hands-on cloud practice, with cost control and teardown documentation.

## Compatibility Constraints

- PostgreSQL SQL and JPA mappings should avoid vendor assumptions beyond PostgreSQL support.
- The API should remain backward-compatible within `/api/v1` once endpoint contracts are documented.

## Deferred Constraints

- Rate limiting is deferred.
- Refresh token lifecycle is deferred.
- Password reset is deferred.
- Secrets Manager integration is deferred.
- Multi-currency behavior is deferred.
- Account statement generation is deferred.
