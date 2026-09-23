# CloudBank Requirement Traceability

| Requirement | Source | Verification method | Test/evaluation | Expected result | Actual result | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| REQ-001 Registration | `requirements.md` | Static + integration | `mvn verify`, `CloudBankApiIntegrationTests` | Valid registration works; duplicate email returns `409` | Valid registration tested; duplicate static only | PARTIAL | Duplicate path implemented in `AuthService`, not directly tested. |
| REQ-002 Password hashing | `requirements.md` | Static | Code review | Password not stored plaintext | Uses `BCryptPasswordEncoder` | PASS | No DB assertion test. |
| REQ-003 Login/JWT | `requirements.md` | Static + integration | `mvn verify` | Login returns signed token | Tested valid login | PARTIAL | Invalid credential test missing. |
| REQ-004 Protected endpoints | `requirements.md` | Integration | `mvn verify` | Missing token returns `401` | Tested | PASS | Invalid/expired token not tested. |
| REQ-005 Ownership | `requirements.md` | Static + integration | `mvn verify` | Cross-user account access denied | Account read denied with `404` | PARTIAL | Deposit/withdraw/history cross-user access not tested. |
| REQ-006 Create accounts | `requirements.md` | Integration | `mvn verify` | Active zero-balance account created | Tested | PASS |  |
| REQ-007 List/get own accounts | `requirements.md` | Static + integration | `mvn verify` | User only receives own accounts | Get tested; list static only | PARTIAL | List endpoint not directly asserted. |
| REQ-008 No direct balance mutation | `requirements.md` | Static | Route review | No public set-balance endpoint | No set-balance endpoint found | PASS |  |
| REQ-009 Deposits | `requirements.md` | Integration | `mvn verify` | Deposit updates balance and transaction | Tested | PASS |  |
| REQ-010 Withdrawals | `requirements.md` | Integration | `mvn verify` | Withdrawal works; insufficient funds `422` | Tested | PASS |  |
| REQ-011 Transfers | `requirements.md` | Integration | `mvn verify` | Atomic debit/credit and records | Basic transfer tested | PARTIAL | Destination balance/credit transaction not asserted directly. |
| REQ-012 Invalid amounts | `requirements.md` | Static | DTO review | Zero/negative return `400` | Bean Validation present | PARTIAL | Not directly tested. |
| REQ-013 Transaction history | `requirements.md` | Integration | `mvn verify` | Authorized history sorted by timestamp | History count tested | PARTIAL | Sort order not asserted. |
| REQ-014 Pagination | `requirements.md` | Static | Service review | Page/size supported and bounded | Implemented | PARTIAL | Not directly tested. |
| REQ-015 Idempotency required | `requirements.md` | Static + integration | `mvn verify` | Missing key `400`; duplicate replay | Duplicate replay tested; missing key static only | PARTIAL | Missing-key test absent. |
| REQ-016 Idempotency conflict | `requirements.md` | Integration | `mvn verify` | Same key/different payload returns `409` | Tested for deposit | PASS | Withdraw/transfer conflicts not tested. |
| REQ-017 Idempotency persistence | `requirements.md` | Static | Entity/repository review | Stores key/hash/status/body/user | Implemented | PASS | No DB-level assertion. |
| REQ-018 Bean Validation | `requirements.md` | Static | DTO review | Invalid fields return `400` | DTO annotations present | PARTIAL | Validation response not directly tested. |
| REQ-019 Centralized exceptions | `requirements.md` | Static | Handler review | Consistent error shape | Implemented for validation/domain/auth | PARTIAL | Malformed JSON likely falls through to generic `500`. |
| REQ-020 OpenAPI | `requirements.md` | Integration | `mvn verify` | `/v3/api-docs` available | Tested | PASS | Schema completeness not deeply inspected. |
| REQ-021 PostgreSQL persistence | `requirements.md` | Static | Config/entity review | PostgreSQL-compatible persistence | Main config uses PostgreSQL | PARTIAL | Automated tests use H2, not PostgreSQL. |
| REQ-022 Docker local setup | `requirements.md` | Static | Compose review | Developer can run DB/API | Compose files present | PARTIAL | Docker CLI unavailable; not executed. |
| REQ-023 Integration tests | `requirements.md` | Test review | `mvn verify` | Core flows covered | Main happy path covered | PARTIAL | Some failure modes missing. |
| REQ-024 GitHub Actions CI | `requirements.md` | Static | Workflow review | Runs build/tests on PR/main | Workflow runs `mvn verify` | PASS | Not observed in GitHub UI here. |
| REQ-025 Terraform AWS scope | `requirements.md`, AC-018 | Static | Terraform review | VPC, ECS service/task, RDS, logs, S3, env vars, SGs | Partial scaffold only | FAIL | Missing ECS service/task, RDS, subnets, env vars. |
| REQ-026 Safe operational logs | `requirements.md` | Static | Code review | Useful logs without secrets | No explicit secret logging found | PARTIAL | Operational request/error logging is minimal. |
| REQ-027 Correlation IDs | `requirements.md` | Static | Filter review | Each request traceable | Correlation header/MDC implemented | PARTIAL | No structured request log using MDC. |
| REQ-028 Local 300 ms target | `requirements.md` | Not tested | None | CRUD under 300 ms | Not measured | NOT TESTED | Needs benchmark or timed integration check. |
