# CloudBank Test Results

## Automated Tests

Command:

```bash
mvn verify
```

Outcome:

- Build success.
- Tests run: 4.
- Failures: 0.
- Errors: 0.
- Skipped: 0.
- Spring Boot executable jar packaged successfully.

Covered by automated tests:

- Application context startup.
- User registration.
- Login and JWT usage.
- Account creation.
- Missing-token protected endpoint rejection.
- Deposit success.
- Deposit duplicate idempotency replay.
- Deposit idempotency conflict.
- Withdrawal success.
- Insufficient-funds withdrawal.
- Transfer success.
- Transaction history basic retrieval.
- Cross-user account read rejection.
- OpenAPI JSON availability.

## Static Checks

- Docker Compose file reviewed but not executed because `docker` is not installed.
- Terraform files reviewed but not validated because `terraform` is not installed.
- Secret-pattern scan found only placeholder `.env.example` content, not committed real secrets.

## Untested / Lightly Tested Areas

- Duplicate registration response.
- Invalid login response.
- Invalid/expired JWT response.
- Missing idempotency key response.
- Zero/negative amount validation response.
- Transaction history sort order and pagination boundaries.
- PostgreSQL-specific JPA behavior and row locking.
- Concurrent withdrawal/transfer behavior.
- Docker local run.
- Terraform validation and AWS plan.
- Performance target.
