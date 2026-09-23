# CloudBank Architecture Decisions

## ADR-001: Use a Modular Monolith

Decision:

- Build CloudBank as one Spring Boot deployable with layered internal modules.

Alternatives considered:

- Microservices for auth, accounts, ledger, and transactions.
- Serverless functions.

Why selected:

- MVP has one bounded domain and one data owner.
- Money movement needs simple local transaction boundaries.
- Portfolio review benefits from clarity over distributed complexity.

Trade-offs:

- Less independent scaling by domain.
- Module boundaries require discipline inside one codebase.

Consequences:

- Easier local development, testing, and deployment.
- Future extraction remains possible if domain complexity grows.

## ADR-002: Use PostgreSQL as the System of Record

Decision:

- Store users, accounts, transactions, transfers, and idempotency records in PostgreSQL.

Alternatives considered:

- MySQL.
- Embedded database only.
- NoSQL document store.

Why selected:

- PRD specifies PostgreSQL.
- Relational transactions and constraints fit money movement.
- RDS PostgreSQL aligns with AWS scope.

Trade-offs:

- Requires schema migration discipline.
- Horizontal write scaling is not addressed in MVP.

Consequences:

- Strong consistency is available for account balance operations.

## ADR-003: Use Row-Level Locking for Balance Updates

Decision:

- Use PostgreSQL row-level locks for accounts during money movement.

Alternatives considered:

- Optimistic locking with retries.
- Serialized database isolation for all money movement.
- Application-level locks.

Why selected:

- Simple and explicit for deposits, withdrawals, and transfers.
- Prevents double-spend scenarios.
- Easy to test with concurrent requests.

Trade-offs:

- Requests touching the same account serialize.
- Poor lock ordering can create deadlocks.

Consequences:

- Transfers must lock accounts in deterministic order.

## ADR-004: Require Idempotency Keys for Money Movement

Decision:

- Deposits, withdrawals, and transfers require `Idempotency-Key`.

Alternatives considered:

- Optional idempotency.
- Server-generated operation IDs.
- Client retries without idempotency.

Why selected:

- PRD requires audit-safe idempotency.
- Client retry safety is essential for money movement.

Trade-offs:

- Clients must generate and store keys.
- Server stores response metadata.

Consequences:

- Duplicate requests can safely replay original responses.

## ADR-005: Scope Idempotency by User, Endpoint, and Key

Decision:

- Unique idempotency scope is authenticated user, endpoint, and idempotency key.

Alternatives considered:

- Global key uniqueness.
- User-only key uniqueness.
- Endpoint-only key uniqueness.

Why selected:

- Prevents cross-user data leakage.
- Avoids accidental conflicts between unrelated endpoints.

Trade-offs:

- Same key can be reused by the same user on different endpoints.
- Endpoint naming must be stable.

Consequences:

- Request hash must include path parameters and body.

## ADR-006: Use JWT for Stateless API Authentication

Decision:

- Authenticate protected endpoints with signed JWT access tokens.

Alternatives considered:

- Server sessions.
- OAuth/OIDC provider.
- API keys.

Why selected:

- PRD specifies JWT.
- Stateless auth fits containerized deployment.
- Avoids external identity provider scope.

Trade-offs:

- Token revocation is not handled in MVP.
- Secret rotation requires operational care.

Consequences:

- Refresh tokens and password reset remain deferred.

## ADR-007: Return 404 for Non-Owned Account Resources

Decision:

- Recommended convention is to return `404` for missing and non-owned account resources.

Alternatives considered:

- Return `403` for known resource owned by another user.

Why selected:

- Avoids confirming whether another user's account exists.
- Common pattern for resource-level authorization.

Trade-offs:

- Slightly less explicit during portfolio review.

Consequences:

- Tests should assert `404` for cross-user account access if this recommendation is adopted.

## ADR-008: Keep AWS Deployment as Terraform Scope

Decision:

- Provide Terraform architecture for ECS Fargate, RDS PostgreSQL, CloudWatch, S3, and networking, without making cloud deployment required for local development.

Alternatives considered:

- Local-only project.
- Full production deployment pipeline.

Why selected:

- PRD asks for hands-on cloud infrastructure practice.
- Avoids making AWS credentials or cost mandatory for development.

Trade-offs:

- Terraform may remain un-applied in some review contexts.

Consequences:

- Documentation must explain cost controls and teardown.

## ADR-009: Defer Asynchronous Processing

Decision:

- Do not introduce message queues or event streams in MVP.

Alternatives considered:

- Publish transaction events.
- Use async ledger projection.

Why selected:

- Requirements are synchronous REST workflows.
- Audit history is persisted transactionally.
- Event-driven ledger replication is explicitly deferred.

Trade-offs:

- No async integration hooks in MVP.

Consequences:

- Future eventing can be added from committed transaction records.

## ADR-010: Use Generated OpenAPI Documentation

Decision:

- Generate OpenAPI docs from Spring controller and DTO metadata.

Alternatives considered:

- Manually maintained OpenAPI file.
- Postman collection only.

Why selected:

- Reduces documentation drift.
- Supports recruiter and developer review.

Trade-offs:

- Controller annotations need maintenance.

Consequences:

- Add smoke test for OpenAPI JSON endpoint.
