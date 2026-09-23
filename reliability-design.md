# CloudBank Reliability Design

Source inputs: `technical-risks.md`, `engineering-spec.md`, `acceptance-criteria.md`.

## 1. Reliability Goals

- Money movement is atomic.
- Duplicate client retries do not duplicate balance changes.
- Concurrent operations on the same account cannot corrupt balances.
- Errors are predictable and recoverable where possible.
- Local and CI test environments are deterministic.

## 2. Transaction Boundaries

Use service-layer `@Transactional` methods for:

- Deposit.
- Withdrawal.
- Transfer.

Each transaction includes:

- Idempotency first-write or replay decision.
- Account row locks.
- Balance updates.
- Transaction record inserts.
- Transfer record insert when applicable.
- Idempotency response metadata.

## 3. Concurrency Control

Recommended approach:

- Use PostgreSQL row-level locking with JPA pessimistic write locks or explicit locking query.

For transfer:

- Load and lock both accounts in deterministic order by account ID.
- Then validate source ownership and sufficient funds.
- Apply debit and credit.

Why:

- Simpler to reason about than optimistic retries for portfolio MVP.
- Prevents double-spend under concurrent withdrawals.

## 4. Idempotency Reliability

Behavior:

- Missing key returns `400`.
- Existing matching key replays stored response.
- Existing conflicting key returns `409`.
- New key executes operation and stores response metadata.

Duplicate-first-request race:

- Unique constraint on `user_id`, `endpoint`, `idempotency_key` is the final guard.
- If two requests race to insert the same idempotency key, one succeeds and the other reloads the record, then replays or conflicts.

## 5. Failure Modes

| Failure | Expected Behavior |
| --- | --- |
| Database unavailable | Return safe server error or `503`; no partial money movement. |
| Lock contention | Request waits until lock timeout; future enhancement can map timeout to `409` or `503`. |
| Insufficient funds | Return `422`; no balance mutation. |
| Idempotency conflict | Return `409`; no money movement. |
| Invalid JWT | Return `401`; no service execution. |
| Unexpected exception during money movement | Transaction rolls back; return safe `500`. |

## 6. Retries and Timeouts

Client retries:

- Allowed for deposits, withdrawals, and transfers only when the same `Idempotency-Key` and same payload are reused.

Server retries:

- Do not automatically retry money movement at the controller layer.
- If database deadlock handling is added, retry only inside a bounded service policy and preserve idempotency semantics.

Timeouts:

- HTTP request timeout is environment-specific.
- Database lock timeout may be configured later.

## 7. Backpressure and Load

MVP does not include rate limiting. The architecture should keep rate limiting addable through:

- Spring filter.
- API gateway or load balancer rules.
- Per-user or per-IP policies.

Database connection pool size should be bounded to avoid overwhelming PostgreSQL.

## 8. Graceful Degradation

- If OpenAPI docs fail, core API should continue where possible.
- If metrics export fails, request handling should continue.
- If database fails, core API cannot perform protected workflows and should fail safely.

## 9. Recovery

- PostgreSQL remains source of truth after restart.
- Stateless API containers can restart without losing account state.
- JWTs remain valid across restarts if signing secret remains unchanged.
- Idempotency records allow safe client retry after network interruptions.

## 10. Operational Runbooks Needed

- Local database startup and reset.
- Failed migration recovery.
- CI integration test failure diagnosis.
- ECS task restart investigation.
- RDS connectivity failure investigation.
- AWS teardown to control cost.
