# CloudBank Defects

## HIGH: Terraform Scope Does Not Satisfy AC-018

- Requirement impacted: REQ-025, AC-018.
- Evidence: `terraform/main.tf` defines VPC, CloudWatch log group, S3 bucket, ECS cluster, and security groups, but explicitly leaves subnets, routing, ECS task definitions, and RDS subnet groups incomplete at lines 66-68. It does not define an RDS instance, ECS service, ECS task definition, environment variables, or complete networking.
- Expected: Terraform defines VPC, ECS Fargate service, RDS PostgreSQL, CloudWatch logs, S3 artifacts, environment variables, and security groups restricting database access.
- Actual: Partial scaffold.
- Release impact: Cloud deployment scope is not implementation-complete.
- Recommended fix: Add subnets, route tables, ECS task/service, task execution role, RDS subnet group, RDS instance, app env vars/secrets references, and validation docs.

## HIGH: JWT Default Secret Can Be Used If Environment Is Missing

- Requirement impacted: SEC-003, SEC-023, security architecture startup validation.
- Evidence: `src/main/resources/application.yml` provides `JWT_SECRET:dev-only-change-me-to-a-long-random-secret` fallback.
- Expected: Non-test/non-local startup should require an environment-specific secret.
- Actual: The app can start with a predictable development secret.
- Release impact: If deployed without overriding `JWT_SECRET`, tokens can be forged.
- Recommended fix: Remove production fallback or add startup validation that rejects known default secrets outside test/local profiles.

## MEDIUM: PostgreSQL-Specific Persistence and Locking Are Not Verified

- Requirement impacted: REQ-021, RISK-001, AC-006 to AC-008.
- Evidence: Automated tests use H2 (`src/test/resources/application-test.yml`) rather than PostgreSQL/Testcontainers. Money movement relies on pessimistic locking in `AccountRepository`.
- Expected: PostgreSQL-compatible persistence and row-locking behavior are verified.
- Actual: Static review only for PostgreSQL; tests run against H2 compatibility mode.
- Release impact: Concurrency and locking correctness remain uncertain under the target database.
- Recommended fix: Add Testcontainers PostgreSQL tests for deposit, withdrawal, transfer, and concurrent double-withdrawal behavior.

## MEDIUM: Framework-Level Bad Requests Are Not Mapped to Documented Error Shape

- Requirement impacted: REQ-019, AC-013.
- Evidence: `GlobalExceptionHandler` handles `ApiException`, `MethodArgumentNotValidException`, auth exceptions, and generic `Exception`; it does not handle `HttpMessageNotReadableException`, type mismatch, or missing request headers/parameters explicitly.
- Expected: Malformed JSON and request parsing failures return stable `400` error responses.
- Actual: Static review indicates malformed input can fall through to generic `500` or framework default handling.
- Release impact: Clients may see incorrect status codes and inconsistent error payloads.
- Recommended fix: Add explicit handlers and tests for malformed JSON, invalid UUID path variables, and missing required headers.

## MEDIUM: Critical Failure-Mode Tests Are Missing

- Requirement impacted: AC-001 to AC-013, SEC-004, SEC-012, SEC-013.
- Evidence: Existing integration tests cover the main happy path plus a few failures, but do not cover duplicate registration, invalid login, invalid/expired JWT, missing idempotency key, invalid amounts, cross-user money movement/history, transfer invalid destination, or pagination boundaries.
- Expected: Core failure modes are covered by integration tests.
- Actual: Coverage is partial.
- Release impact: Regression risk remains in security and financial failure paths.
- Recommended fix: Add focused integration tests for each missing acceptance criterion.

## LOW: Observability Is Partial

- Requirement impacted: REQ-026, REQ-027, observability requirements.
- Evidence: `CorrelationIdFilter` adds `X-Correlation-ID` and MDC, but there is no structured request log including method, path, status, duration, or operation outcome.
- Expected: Logs include correlation ID and enough operational context to debug failures.
- Actual: Correlation primitives exist, operational logging is minimal.
- Release impact: Debuggability is limited.
- Recommended fix: Add request logging filter or interceptor and safe money-movement outcome logs.

## LOW: Performance Requirement Is Untested

- Requirement impacted: REQ-028.
- Evidence: No performance test or timed local CRUD measurement was run.
- Expected: Standard local CRUD operations complete within 300 ms under normal development load.
- Actual: Not measured.
- Release impact: Performance claim is unsupported.
- Recommended fix: Add a local performance smoke test or document that performance is not yet validated.
