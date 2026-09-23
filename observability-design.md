# CloudBank Observability Design

Source input: `observability-requirements.md`.

## 1. Observability Goals

- Debug authentication, validation, account access, and money movement failures.
- Correlate client errors with server logs.
- Support local development, CI diagnosis, and AWS CloudWatch inspection.
- Avoid leaking secrets.

## 2. Correlation IDs

Behavior:

- Read inbound `X-Correlation-ID` if present and valid.
- Generate a new correlation ID if missing.
- Add correlation ID to logging context.
- Return correlation ID in error responses.
- Optionally return correlation ID in all responses.

## 3. Logs

Request logs should include:

- Timestamp.
- Level.
- Correlation ID.
- HTTP method.
- Path template where available.
- Response status.
- Duration.
- Authenticated user ID when safe and available.

Business logs should include:

- Money movement operation type.
- Account IDs when useful.
- Transaction or transfer ID after success.
- Idempotency replay or conflict indicators.

Never log:

- Passwords.
- Password hashes.
- Raw JWTs.
- JWT signing secret.
- Database password.

## 4. Metrics

Recommended metrics:

- `http.server.requests` by method, route, status.
- Request latency percentiles.
- Authentication failures.
- Deposit success/failure count.
- Withdrawal success/failure count.
- Transfer success/failure count.
- Idempotency replay count.
- Idempotency conflict count.
- Database connection pool metrics.

Spring Actuator and Micrometer are recommended but optional for MVP.

## 5. Health Checks

Recommended endpoints if Actuator is included:

- `/actuator/health/liveness`.
- `/actuator/health/readiness`.

Readiness should check database connectivity.

## 6. Dashboards

For AWS practice, a useful dashboard would show:

- ECS task count and restarts.
- HTTP error rates.
- Request latency.
- RDS CPU, connections, and storage.
- Application log error count.

Dashboards are optional for MVP but the metrics/logging architecture should not block them.

## 7. Alerts

Not required for MVP. Future alerts:

- High `5xx` rate.
- High authentication failure rate.
- Repeated idempotency conflicts.
- Database connection failures.
- ECS task restarts.
- RDS capacity pressure.

## 8. Audit Signals

Persisted transaction history is the core audit artifact for successful money movement.

Logs should additionally record:

- Insufficient funds rejection.
- Idempotency replay.
- Idempotency conflict.
- Ownership violation attempt without exposing sensitive details.

## 9. CI Observability

CI should expose:

- Build logs.
- Test reports.
- Integration test database startup logs where available.

Failed tests should include enough assertion context to identify the broken requirement.
