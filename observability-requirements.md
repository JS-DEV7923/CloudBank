# CloudBank Observability Requirements

Source of truth: `PRD.md`

## Logging

- OBS-001: Each request should have a correlation ID.
- OBS-002: Logs should include timestamp, level, correlation ID, HTTP method, path, status, and duration.
- OBS-003: Authentication failures should be logged at an appropriate level without exposing credentials.
- OBS-004: Money movement failures should include operation type and non-sensitive identifiers.
- OBS-005: Errors should include enough context to debug without logging secrets or stack traces to clients.

## Audit Events

The system should persist transaction records as audit-safe domain events for:

- Deposit completed.
- Withdrawal completed.
- Withdrawal rejected for insufficient funds.
- Transfer completed.
- Transfer rejected for insufficient funds.
- Idempotent retry returned.
- Idempotency key conflict rejected.

Rejected operations do not necessarily need to create `Transaction` rows in MVP, but application logs should make them diagnosable.

## Metrics

Recommended application metrics:

- Request count by endpoint, method, and status.
- Request latency by endpoint.
- Authentication failure count.
- Deposit count and failure count.
- Withdrawal count and failure count.
- Transfer count and failure count.
- Idempotency replay count.
- Idempotency conflict count.
- Database query or connection pool health metrics if actuator is enabled.

## Health Checks

- Application liveness endpoint if Spring Actuator is included.
- Database readiness check if Spring Actuator is included.
- CI should validate application context startup.

## CloudWatch Requirements

- Terraform scope must include CloudWatch log groups for ECS task logs.
- ECS task definition should route application logs to CloudWatch.
- Log retention should be configurable.

## Alerting Recommendations

Alerting is not required for MVP, but the AWS scope should be compatible with future alerts for:

- Elevated `5xx` errors.
- Repeated authentication failures.
- Database connection failures.
- ECS task restarts.
- RDS CPU, storage, or connection saturation.

## Debugging Hooks

- Include correlation ID in error response when available.
- Include correlation ID in logs for matching client failures to server-side logs.
- Keep stable application error codes for support and test assertions.

## Success Indicators

- Integration test failures can be traced through logs.
- Local developer can identify validation, auth, and database errors quickly.
- Deployed ECS logs are available in CloudWatch.
