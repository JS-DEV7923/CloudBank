# CloudBank Integrations

Source of truth: `PRD.md`

## Required Integrations

### PostgreSQL

- Purpose: primary persistence for users, accounts, transactions, transfers, and idempotency records.
- Environment: local Docker Compose, integration tests, AWS RDS scope.
- Data owner: CloudBank API.
- Required behavior: transactional writes, indexes for identity/account/history lookup, safe concurrent updates.

### JWT Authentication

- Purpose: stateless authentication for protected REST endpoints.
- Provider: internal Spring Security JWT implementation.
- External dependency: signing secret provided through environment variables.
- Required behavior: validate token signature, expiration, and authenticated user identity on protected endpoints.

### Docker Compose

- Purpose: local development runtime for PostgreSQL and optionally the API.
- Inputs: local environment variables for database credentials, JWT secret, active profile.
- Required behavior: allow developer to start dependencies and run the API quickly.

### OpenAPI/Swagger

- Purpose: developer and recruiter review of API contracts.
- Required behavior: expose endpoint documentation, schemas, status codes, auth requirements, and error examples.

### GitHub Actions

- Purpose: continuous integration for build and tests.
- Trigger: pull requests and pushes to main branch.
- Required behavior: set up Java, run build, run tests, report failures.

### AWS Terraform Scope

- Purpose: demonstrate cloud infrastructure practice.
- Components:
  - VPC and networking.
  - ECS Fargate service.
  - RDS PostgreSQL.
  - CloudWatch logs.
  - S3 artifacts.
  - Environment variables.
  - Security groups.
- Required behavior: describe deployable infrastructure without requiring real banking integrations.

## Explicitly Out-of-Scope Integrations

- ACH, cards, wires, payment processors, or banking cores.
- KYC, AML, fraud detection, or credit scoring services.
- Email or SMS providers.
- Admin dashboard or frontend application.
- External ledger replication.

## Manual Dependencies

- A developer or reviewer must provide local environment variables.
- A GitHub account with repository write access is required to push CI configuration.
- AWS credentials and account permissions are required only if Terraform is applied.

## Integration Risks

| Integration | Risk | Mitigation |
| --- | --- | --- |
| PostgreSQL | Schema or locking decisions can cause incorrect balances under concurrency. | Use database transactions and row-level or optimistic locking. |
| JWT | Misconfigured signing secret can invalidate auth or weaken security. | Use environment variables and explicit startup validation. |
| Docker Compose | Local setup can drift from CI or AWS runtime. | Keep environment variables documented and use consistent profiles. |
| GitHub Actions | Integration tests may need database service setup. | Use Testcontainers or CI PostgreSQL service. |
| AWS Terraform | Running cloud resources can create cost. | Document minimal sizing and teardown steps. |
