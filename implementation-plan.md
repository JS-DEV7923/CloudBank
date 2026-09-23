# CloudBank Implementation Plan

Source of truth: `PRD.md`

## Implementation Sequence

1. Foundation and project scaffolding.
2. Persistence model and database migrations.
3. Authentication and security.
4. Account management.
5. Money movement and idempotency.
6. Transaction history.
7. Error handling, validation, and OpenAPI.
8. Tests and CI.
9. Docker and Terraform deployment scope.

## Tasks

### ENG-001: Initialize Spring Boot Project

- Objective: Create the application foundation.
- Description: Set up Java Spring Boot project with dependencies for Web, Validation, Security, Data JPA, PostgreSQL, testing, and OpenAPI.
- Dependencies: None.
- Affected components: build config, application config, package structure.
- Expected behavior: Application starts with base health or context test.
- Acceptance criteria: Project builds and Spring context loads.
- Tests required: Context load test.
- Definition of done: Build file, application class, profiles, and base package structure exist.

### ENG-002: Configure PostgreSQL Persistence

- Objective: Connect the API to PostgreSQL.
- Description: Configure datasource, JPA settings, local profile, and database migration approach if used.
- Dependencies: ENG-001.
- Affected components: config, repository layer, Docker Compose.
- Expected behavior: Application can connect to PostgreSQL locally.
- Acceptance criteria: Repository integration test can persist and read an entity.
- Tests required: Persistence smoke test.
- Definition of done: Database config documented and testable.

### ENG-003: Implement Domain Entities

- Objective: Model users, accounts, transactions, transfers, and idempotency records.
- Description: Create JPA entities, enums, repositories, indexes, timestamps, and relationships.
- Dependencies: ENG-002.
- Affected components: domain, repository, database schema.
- Expected behavior: Entities persist with required constraints.
- Acceptance criteria: Unique email, unique account number, idempotency uniqueness, and account-history indexes are represented.
- Tests required: Repository tests for key constraints.
- Definition of done: Domain model supports all MVP workflows.

### ENG-004: Implement Registration and Password Hashing

- Objective: Allow user creation securely.
- Description: Add registration DTOs, service, controller, password encoder, and duplicate email handling.
- Dependencies: ENG-003.
- Affected components: auth controller, user service, security config, exception handling.
- Expected behavior: Valid users register; duplicate emails fail.
- Acceptance criteria: `POST /api/v1/auth/register` returns created user without password fields.
- Tests required: Registration integration tests.
- Definition of done: Registration path is validated, secure, and documented.

### ENG-005: Implement Login and JWT Authentication

- Objective: Authenticate users and protect endpoints.
- Description: Add login endpoint, JWT service, security filter, authentication provider, and protected route behavior.
- Dependencies: ENG-004.
- Affected components: security, auth controller, config.
- Expected behavior: Valid login returns token; protected endpoints require token.
- Acceptance criteria: Missing or invalid token returns `401`; valid token authenticates user.
- Tests required: Login and protected endpoint tests.
- Definition of done: JWT auth works consistently for protected APIs.

### ENG-006: Implement Centralized Error Handling

- Objective: Standardize API failures.
- Description: Add global exception handler, error response DTO, validation mapping, and application exception types.
- Dependencies: ENG-001.
- Affected components: exception package, controllers.
- Expected behavior: Handled errors share a consistent response shape.
- Acceptance criteria: Validation, auth, duplicate, not found, conflict, and insufficient funds errors map to expected statuses.
- Tests required: Error response integration tests.
- Definition of done: Error handling contract is stable and documented.

### ENG-007: Implement Account Management

- Objective: Enable account creation and retrieval.
- Description: Add account service, controller, ownership checks, account number generation, account DTOs.
- Dependencies: ENG-005, ENG-006.
- Affected components: account controller, account service, account repository.
- Expected behavior: Authenticated users create and retrieve only their accounts.
- Acceptance criteria: Cross-user account access is rejected or hidden according to final API convention.
- Tests required: Account integration tests and ownership tests.
- Definition of done: Account APIs satisfy AC-004 and AC-005.

### ENG-008: Implement Idempotency Service

- Objective: Provide reusable idempotency support for money movement.
- Description: Add idempotency key validation, request hashing, lookup, conflict detection, and response replay support.
- Dependencies: ENG-003, ENG-006.
- Affected components: idempotency service, repository, money movement services.
- Expected behavior: Same key and request replays original response; same key and different request returns `409`.
- Acceptance criteria: Idempotency behavior passes duplicate and conflict tests.
- Tests required: Idempotency service unit tests and endpoint integration tests.
- Definition of done: Money movement endpoints can use idempotency consistently.

### ENG-009: Implement Deposits

- Objective: Support atomic deposits.
- Description: Add deposit endpoint, service logic, validation, account locking/versioning, transaction record creation, and idempotency.
- Dependencies: ENG-007, ENG-008.
- Affected components: transaction controller/service, account repository, transaction repository.
- Expected behavior: Positive deposit increases balance and creates transaction once.
- Acceptance criteria: AC-006 and idempotency AC-010 to AC-012 pass.
- Tests required: Deposit success, validation, ownership, duplicate retry, conflict retry.
- Definition of done: Deposit workflow is transactional and idempotent.

### ENG-010: Implement Withdrawals

- Objective: Support atomic withdrawals with sufficient-funds checks.
- Description: Add withdrawal endpoint, service logic, account locking/versioning, transaction record creation, and idempotency.
- Dependencies: ENG-009.
- Affected components: transaction controller/service, account repository, transaction repository.
- Expected behavior: Valid withdrawal decreases balance; insufficient funds fail without mutation.
- Acceptance criteria: AC-007 and idempotency AC-010 to AC-012 pass.
- Tests required: Withdrawal success, insufficient funds, validation, duplicate retry, conflict retry.
- Definition of done: Withdrawal workflow is transactional and idempotent.

### ENG-011: Implement Transfers

- Objective: Support atomic transfers between accounts.
- Description: Add transfer endpoint, service logic, ownership checks, account locking/versioning for both accounts, transfer record, debit and credit transaction records, and idempotency.
- Dependencies: ENG-010.
- Affected components: transfer controller/service, account repository, transaction repository, transfer repository.
- Expected behavior: Source debit and destination credit commit together.
- Acceptance criteria: AC-008 and idempotency AC-010 to AC-012 pass.
- Tests required: Transfer success, insufficient funds, invalid destination, unauthorized source, duplicate retry, conflict retry.
- Definition of done: Transfer workflow is atomic and idempotent.

### ENG-012: Implement Transaction History

- Objective: Expose account activity.
- Description: Add paginated transaction history endpoint with timestamp sorting and ownership checks.
- Dependencies: ENG-009, ENG-010, ENG-011.
- Affected components: transaction controller/service/repository.
- Expected behavior: Authenticated users can view paginated history for owned accounts.
- Acceptance criteria: AC-009 passes.
- Tests required: History sorting, pagination, ownership tests.
- Definition of done: Transaction history is accurate, authorized, and bounded.

### ENG-013: Add OpenAPI Documentation

- Objective: Make API reviewable.
- Description: Configure OpenAPI and annotate controllers/DTOs/errors as needed.
- Dependencies: ENG-004 through ENG-012.
- Affected components: controllers, DTOs, OpenAPI config.
- Expected behavior: Docs list all public endpoints, schemas, auth, and errors.
- Acceptance criteria: AC-014 passes through manual review or generated spec inspection.
- Tests required: OpenAPI endpoint smoke test.
- Definition of done: Swagger/OpenAPI docs are usable by reviewers.

### ENG-014: Add Docker Compose Local Runtime

- Objective: Make local development fast.
- Description: Add Docker Compose for PostgreSQL and optionally API, plus `.env.example` and run instructions.
- Dependencies: ENG-002.
- Affected components: Docker files, docs, local config.
- Expected behavior: Developer can start required services locally.
- Acceptance criteria: AC-017 satisfied.
- Tests required: Manual local startup verification.
- Definition of done: Local run path is documented and reproducible.

### ENG-015: Add Integration Test Suite

- Objective: Validate core flows end to end.
- Description: Add integration tests for auth, accounts, deposits, withdrawals, transfers, transaction history, and idempotency.
- Dependencies: ENG-004 through ENG-012.
- Affected components: test suite, CI support.
- Expected behavior: Tests cover critical MVP behavior.
- Acceptance criteria: AC-015 passes.
- Tests required: Integration tests themselves.
- Definition of done: Test suite runs reliably locally.

### ENG-016: Add GitHub Actions CI

- Objective: Automate build and test validation.
- Description: Add workflow for pull requests and main pushes.
- Dependencies: ENG-015.
- Affected components: `.github/workflows`, test config.
- Expected behavior: CI runs build and tests.
- Acceptance criteria: AC-016 passes.
- Tests required: Workflow run.
- Definition of done: CI status reflects project health.

### ENG-017: Add Terraform AWS Infrastructure Scope

- Objective: Document and define cloud deployment architecture.
- Description: Add Terraform for VPC, ECS Fargate, RDS PostgreSQL, CloudWatch logs, S3 artifacts, environment variables, and security groups.
- Dependencies: ENG-014, Docker image configuration.
- Affected components: `infra` or `terraform` directory, deployment docs.
- Expected behavior: Infrastructure scope is clear and reviewable.
- Acceptance criteria: AC-018 satisfied by Terraform files and documentation.
- Tests required: `terraform fmt` and `terraform validate` where provider setup allows.
- Definition of done: AWS scope is represented with cost and teardown notes.

### ENG-018: Documentation Polish

- Objective: Make the repository recruiter-reviewable.
- Description: Add README sections for purpose, architecture, local setup, API docs, tests, CI, deployment scope, and non-goals.
- Dependencies: ENG-013, ENG-014, ENG-016, ENG-017.
- Affected components: README and docs.
- Expected behavior: Reviewer understands how to run and evaluate the project.
- Acceptance criteria: Success metrics from PRD section 6 are addressed.
- Tests required: Documentation walkthrough.
- Definition of done: Project can be understood without private context.

## Quality Gate

- Every important PRD requirement is represented in `requirements.md`.
- Every requirement has testable behavior or is flagged as ambiguous.
- Blocking ambiguities: none identified for MVP planning.
- Non-blocking ambiguities are captured in `requirements.md` and `technical-risks.md`.
- Dependencies are identified in the task list.
- Non-functional requirements are captured in `technical-constraints.md`, `security-requirements.md`, and `observability-requirements.md`.
- Security requirements are considered.
- Observability requirements are considered.
- Implementation tasks include dependencies and definitions of done.
