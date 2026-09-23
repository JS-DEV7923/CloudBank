# CloudBank Technical Risks

Source of truth: `PRD.md`

| Risk ID | Risk | Impact | Likelihood | Blocking | Mitigation |
| --- | --- | --- | --- | --- | --- |
| RISK-001 | Concurrent withdrawals or transfers can create incorrect balances. | High | Medium | Yes for money movement release | Use database transactions plus row-level locking or optimistic locking; add concurrency-oriented tests. |
| RISK-002 | Duplicate client retries can duplicate deposits or transfers. | High | Medium | Yes for money movement release | Require idempotency keys and persist request hash plus response metadata. |
| RISK-003 | Idempotency implementation stores stale or inconsistent responses if transaction boundaries are wrong. | High | Medium | Yes for money movement release | Persist idempotency record in the same transaction as business operation or use carefully designed transaction ordering. |
| RISK-004 | JWT security misconfiguration can allow unauthorized access or break valid users. | High | Medium | Yes for protected API release | Centralize security configuration, test protected endpoints, validate secret presence at startup. |
| RISK-005 | Ownership checks can be missed on nested resources. | High | Medium | Yes | Put ownership checks in service methods and cover with integration tests. |
| RISK-006 | Monetary arithmetic can lose precision if wrong numeric types are used. | High | Low | Yes | Use `BigDecimal` and fixed currency scale; reject invalid amounts. |
| RISK-007 | Error handling can leak sensitive implementation details. | Medium | Medium | No | Use global exception handler and avoid returning stack traces. |
| RISK-008 | OpenAPI docs can drift from implementation. | Medium | Medium | No | Generate docs from controller annotations and verify key endpoints during review. |
| RISK-009 | Integration tests can be flaky if database lifecycle is unreliable. | Medium | Medium | No | Prefer Testcontainers PostgreSQL or a deterministic CI database service. |
| RISK-010 | Terraform can incur unexpected AWS costs. | Medium | Medium | No | Use minimal resource sizing, document teardown, avoid applying infrastructure by default. |
| RISK-011 | Scope can expand beyond portfolio MVP. | Medium | High | No | Keep deferred features out of the first implementation sequence. |
| RISK-012 | Local Docker setup can diverge from CI and cloud profiles. | Medium | Medium | No | Document profiles and reuse environment variable names consistently. |

## Key Engineering Decisions Needed

| Decision ID | Decision | Recommended MVP Choice | Status |
| --- | --- | --- | --- |
| DEC-001 | Balance concurrency strategy | Row-level locking for money movement account reads | Recommended |
| DEC-002 | Default currency | `USD` | Recommended |
| DEC-003 | Account identifier strategy | UUID primary key plus unique generated account number | Recommended |
| DEC-004 | Ownership violation response | Prefer `404` to avoid resource disclosure, or `403` for clarity in portfolio docs | Needs final API convention |
| DEC-005 | Idempotency retention | No expiration in MVP | Recommended |
| DEC-006 | Integration test database | Testcontainers PostgreSQL | Recommended |

## Blocker Assessment

No current ambiguity blocks implementation planning. `DEC-004` should be finalized before endpoint tests are locked, because it affects expected status codes for cross-user access attempts.
