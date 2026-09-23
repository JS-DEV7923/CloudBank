# CloudBank - Spring Boot Banking API

CloudBank is a production-style banking backend built with Java, Spring Boot, REST APIs, PostgreSQL, JWT authentication, Docker, GitHub Actions, and Terraform-scoped AWS infrastructure.

The project demonstrates secure account ownership, transactional deposits, withdrawals, transfers, transaction history, audit-safe idempotency keys, centralized errors, OpenAPI documentation, and integration tests.

## API

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/accounts`
- `GET /api/v1/accounts`
- `GET /api/v1/accounts/{accountId}`
- `POST /api/v1/accounts/{accountId}/deposits`
- `POST /api/v1/accounts/{accountId}/withdrawals`
- `POST /api/v1/transfers`
- `GET /api/v1/accounts/{accountId}/transactions`

Money movement endpoints require the `Idempotency-Key` header.

## Local Development

Start PostgreSQL:

```bash
docker compose up postgres
```

Run the API:

```bash
mvn spring-boot:run
```

OpenAPI docs:

```text
http://localhost:8080/swagger-ui.html
```

## Tests

```bash
mvn test
```

The integration tests use H2 in PostgreSQL compatibility mode for fast local and CI execution.

## Docker

Build the jar and run the full stack:

```bash
mvn package
docker compose up --build
```

## Configuration

Copy `.env.example` to `.env` for local Docker use. Do not commit real `.env` files or secrets.

Required runtime variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_SECONDS`

## AWS Scope

The `terraform/` directory contains a reviewable AWS scaffold for VPC, ECS Fargate, RDS PostgreSQL, CloudWatch logs, S3 artifacts, and security groups. Review costs before applying.

## Documentation

Planning and architecture artifacts are included in the repository:

- `PRD.md`
- `requirements.md`
- `engineering-spec.md`
- `api-contract.md`
- `data-model.md`
- `architecture.md`
- `implementation-plan.md`

## Non-Goals

CloudBank does not integrate with real payment rails, ACH, cards, KYC, AML, fraud detection, or regulated banking systems.
