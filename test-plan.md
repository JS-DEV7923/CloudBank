# CloudBank Validation Test Plan

## Scope

Validation covers the implemented Spring Boot API against:

- `PRD.md`
- `requirements.md`
- `acceptance-criteria.md`
- `engineering-spec.md`
- `architecture.md`
- `api-contract.md`
- `data-model.md`
- `implementation-plan.md`
- `security-requirements.md`
- `observability-requirements.md`

## Verification Methods

- Static review of controllers, services, repositories, security, entities, Docker, CI, Terraform, and tests.
- Automated build and integration test execution with `mvn verify`.
- Static security review for authentication, authorization, idempotency, secrets, and error handling.
- Static infrastructure review for Docker Compose, GitHub Actions, and Terraform scope.
- Secret-pattern scan with `rg`.

## Environment

- Local macOS workspace.
- Java 26 runtime executing Maven build with Maven compiler release set to Java 21.
- H2 in PostgreSQL compatibility mode for automated integration tests.
- Docker CLI unavailable in this environment.
- Terraform CLI unavailable in this environment.

## Exclusions

- No live PostgreSQL/Testcontainers execution.
- No Docker runtime execution.
- No Terraform `fmt`, `init`, `validate`, `plan`, or AWS apply.
- No load or concurrency benchmark.
- No AI evaluation, because no AI behavior exists in the implemented product.
