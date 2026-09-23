# CloudBank Integration Design

Source input: `integrations.md`.

## 1. PostgreSQL

Purpose:

- Primary system of record for all CloudBank data.

Protocol:

- JDBC through Spring Data JPA / Hibernate.

Credentials:

- Supplied through environment variables.
- Never committed to repository.

Timeouts and retries:

- Use connection pool defaults initially.
- Do not automatically retry money movement transactions at the HTTP layer because retries must be client-driven with idempotency keys.
- Database lock timeout may be configured later if concurrency tests show waits are too long.

Testing:

- Prefer Testcontainers PostgreSQL for integration tests.

Fallback:

- If database is unavailable, protected API operations fail with safe `503 Service Unavailable` or generic `500` depending on exception handling maturity.

## 2. JWT Authentication

Purpose:

- Stateless user authentication for protected endpoints.

Protocol:

- `Authorization: Bearer <token>` HTTP header.

Secrets:

- `JWT_SECRET` or equivalent environment variable.
- Startup should fail if secret is missing in non-test profiles.

Testing:

- Integration tests for valid token, invalid token, expired token if token utilities support it, and missing token.

Fallback:

- Invalid or unavailable JWT verification returns `401`.

## 3. Docker Compose

Purpose:

- Local developer runtime for PostgreSQL and optionally the API.

Inputs:

- Database name, username, password.
- API datasource URL.
- JWT secret.
- Active Spring profile.

Fallback:

- Developers can run the API from IDE while PostgreSQL runs in Compose.

## 4. OpenAPI / Swagger

Purpose:

- Human-readable and machine-readable API documentation.

Protocol:

- HTTP endpoint, usually `/swagger-ui.html` and `/v3/api-docs`.

Security:

- Can be public in local/dev profile.
- In production-like deployments, docs exposure should be explicitly configured.

Testing:

- Smoke test confirms OpenAPI JSON endpoint is available.

## 5. GitHub Actions

Purpose:

- Automated build and tests.

Trigger:

- Pull requests.
- Pushes to main.

Credentials:

- No production secrets required for MVP tests.

Testing:

- CI should run the same build command documented for local development.
- Integration tests should provision PostgreSQL through Testcontainers or workflow service container.

## 6. AWS Infrastructure

Purpose:

- Demonstrate production-style deployment scope.

Components:

- VPC with public and private subnets.
- Optional Application Load Balancer.
- ECS Fargate cluster, task definition, and service.
- RDS PostgreSQL.
- CloudWatch log groups.
- S3 artifact bucket.
- Security groups.

Credentials:

- AWS credentials are only required to plan/apply Terraform.
- Terraform variables should avoid hardcoded secrets.

Fallback:

- AWS deployment is not required for local feature development.
- Terraform should include teardown documentation to reduce cost risk.

## 7. Explicit Non-Integrations

- No external payment processor.
- No ACH, card, or wire provider.
- No third-party identity provider.
- No email or SMS service.
- No fraud, KYC, AML, or credit scoring provider.
