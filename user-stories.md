# CloudBank User Stories

Source of truth: `PRD.md`

## Epic AUTH: Authentication and User Identity

| Story ID | Story | Priority | Dependencies | Source |
| --- | --- | --- | --- | --- |
| AUTH-001 | As a new user, I want to register with my email, name, and password so that I can access CloudBank. | Must | None | PRD 8, 9.1 |
| AUTH-002 | As a registered user, I want to log in and receive a JWT so that I can call protected API endpoints. | Must | AUTH-001 | PRD 8, 9.1 |
| AUTH-003 | As an API user, I want protected endpoints to reject missing or invalid tokens so that account data stays private. | Must | AUTH-002 | PRD 9.1, 12 |

## Epic ACCT: Account Management

| Story ID | Story | Priority | Dependencies | Source |
| --- | --- | --- | --- | --- |
| ACCT-001 | As an authenticated user, I want to create a bank account so that I can hold funds. | Must | AUTH-002 | PRD 8, 9.2 |
| ACCT-002 | As an authenticated user, I want to list my accounts so that I can review my banking profile. | Must | ACCT-001 | PRD 8, 9.2 |
| ACCT-003 | As an authenticated user, I want to retrieve a specific account I own so that I can see balance and status. | Must | ACCT-001 | PRD 9.2, 10 |
| ACCT-004 | As a user, I want other users blocked from my accounts so that my banking data remains private. | Must | AUTH-002, ACCT-001 | PRD 8, 12, 16 |

## Epic TXN: Money Movement

| Story ID | Story | Priority | Dependencies | Source |
| --- | --- | --- | --- | --- |
| TXN-001 | As an authenticated user, I want to deposit money into my account so that my balance increases. | Must | ACCT-001 | PRD 8, 9.3 |
| TXN-002 | As an authenticated user, I want to withdraw money from my account so that my balance decreases when funds are available. | Must | ACCT-001 | PRD 8, 9.4 |
| TXN-003 | As an authenticated user, I want withdrawals rejected when funds are insufficient so that balances cannot go negative. | Must | TXN-002 | PRD 9.4, 16 |
| TXN-004 | As an authenticated user, I want to transfer funds to another account so that money moves atomically between accounts. | Must | ACCT-001 | PRD 8, 9.5 |
| TXN-005 | As an authenticated user, I want transaction history for an account so that I can audit activity. | Must | TXN-001, TXN-002, TXN-004 | PRD 8, 9.6 |
| TXN-006 | As an API client, I want paginated transaction history so that large histories remain efficient. | Should | TXN-005 | PRD 9.6, 12 |

## Epic IDEMP: Idempotency and Reliability

| Story ID | Story | Priority | Dependencies | Source |
| --- | --- | --- | --- | --- |
| IDEMP-001 | As an API client, I want to send an idempotency key with money movement requests so that retries do not duplicate transactions. | Must | TXN-001 | PRD 8, 9.7 |
| IDEMP-002 | As an API client, I want duplicate retries with the same request to return the original result so that network retry behavior is safe. | Must | IDEMP-001 | PRD 9.7, 16 |
| IDEMP-003 | As the system, I want conflicting reuse of an idempotency key rejected so that clients cannot accidentally mask different operations. | Must | IDEMP-001 | PRD 9.7, 16 |

## Epic API: API Quality and Documentation

| Story ID | Story | Priority | Dependencies | Source |
| --- | --- | --- | --- | --- |
| API-001 | As a developer, I want request validation errors to be clear and consistent so that client integrations are easier to debug. | Must | None | PRD 7, 13 |
| API-002 | As a developer, I want centralized exception handling so that all failures use a predictable response shape. | Must | None | PRD 7, 13 |
| API-003 | As a reviewer, I want OpenAPI docs so that I can inspect endpoints, schemas, auth, and errors without reading all code. | Must | API-001, API-002 | PRD 9.8, 16 |

## Epic DEVOPS: Local Development, CI, and Cloud Scope

| Story ID | Story | Priority | Dependencies | Source |
| --- | --- | --- | --- | --- |
| DEVOPS-001 | As a developer, I want Docker Compose local setup so that I can run the API and PostgreSQL quickly. | Must | None | PRD 6, 15 |
| DEVOPS-002 | As a maintainer, I want GitHub Actions CI so that builds and tests run on pull requests and main pushes. | Must | Test suite | PRD 14, 16 |
| DEVOPS-003 | As a reviewer, I want Terraform AWS infrastructure scope so that I can see how the API would run on ECS Fargate and RDS. | Should | Docker image configuration | PRD 15, 16 |

## Epic OBS: Observability

| Story ID | Story | Priority | Dependencies | Source |
| --- | --- | --- | --- | --- |
| OBS-001 | As an operator, I want request correlation IDs in logs so that individual API calls are traceable. | Should | API filter layer | PRD 12 |
| OBS-002 | As an operator, I want CloudWatch log configuration in the AWS scope so that deployed containers can be debugged. | Should | DEVOPS-003 | PRD 15 |
