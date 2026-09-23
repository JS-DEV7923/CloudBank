# CloudBank Security Test Results

## Checks Performed

- Static review of Spring Security configuration.
- Static review of JWT implementation.
- Static review of password hashing.
- Static review of authorization checks for account-scoped resources.
- Static review of error handling and secret exposure.
- Secret-pattern scan using `rg`.
- Automated test for missing-token protected endpoint access.
- Automated test for cross-user account read denial.

## Passing Evidence

- Protected endpoints require authentication except auth/docs/health routes.
- Passwords are hashed with `BCryptPasswordEncoder`.
- API responses do not expose `passwordHash`.
- Money movement services check source/owned account access.
- Idempotency records are scoped by authenticated user, endpoint, and key.
- Cross-user account read returns `404`.
- Secret scan found no committed private keys or real credentials.

## Findings

- `application.yml` includes a default JWT secret (`dev-only-change-me-to-a-long-random-secret`). This is acceptable for local convenience only if production startup enforces a real secret. The current implementation does not fail startup when the default is used.
- Malformed JSON and other framework-level request parsing errors are not explicitly mapped to the documented error shape; static review indicates they may become generic `500` responses.
- Authorization tests do not yet cover cross-user deposit, withdrawal, transfer source, or transaction history access.
- Invalid and expired JWT cases are not directly tested.

## Recommendation

Do not treat the current build as production-security-ready until the JWT default secret behavior and missing security tests are addressed.
