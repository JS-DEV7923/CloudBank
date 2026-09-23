# CloudBank Spring Boot Banking API PRD

## 1. Product Overview

CloudBank is a production-style banking backend API built with Java, Spring Boot, PostgreSQL, JWT authentication, Docker, and AWS deployment infrastructure. The product demonstrates a secure, auditable, recruiter-reviewable backend system for core banking workflows: user registration, authentication, account management, deposits, withdrawals, transfers, transaction history, and idempotent request handling.

This is not intended to process real money or operate as a regulated financial institution. It is a hands-on engineering project that mirrors real backend concerns such as security, validation, auditability, consistency, observability, cloud deployment, and CI automation.

## 2. Problem Statement

Backend engineering candidates often need more than CRUD examples to demonstrate production readiness. Recruiters and technical reviewers want to see whether a candidate can design secure APIs, model financial workflows, handle concurrency and idempotency, test critical flows, and deploy cloud infrastructure in a realistic way.

CloudBank solves this by packaging a realistic banking domain into a polished backend project with clear API documentation, integration tests, authentication, centralized error handling, and infrastructure-as-code deployment.

## 3. Target Users

### Primary Users

- Backend engineering candidate building a portfolio project.
- Recruiters and hiring managers reviewing the candidate's technical depth.
- Technical interviewers evaluating API design, security, testing, and cloud deployment skills.

### Secondary Users

- Developers learning Spring Boot, PostgreSQL, JWT, Docker, and AWS ECS/RDS.
- Engineers using the project as a reference architecture for REST API design.

## 4. Goals

- Provide a secure REST API for user, account, transaction, and transfer workflows.
- Demonstrate production-style backend design using Spring Boot and PostgreSQL.
- Support JWT-based authentication and authorization.
- Ensure money movement operations are auditable, validated, transactional, and idempotent.
- Include OpenAPI documentation for easy recruiter and developer review.
- Include integration tests for core banking flows.
- Provide Docker-based local development setup.
- Provide AWS deployment scope using Terraform-managed infrastructure.
- Include GitHub Actions CI for automated build and test validation.

## 5. Non-Goals

- Real banking integration, payment rail integration, ACH, cards, wires, or external ledger systems.
- KYC, AML, credit scoring, loan servicing, or fraud detection.
- Mobile or web frontend application.
- Multi-currency support in the MVP.
- Real customer onboarding or production financial compliance.
- Complex bank employee/admin operations beyond basic portfolio demonstration needs.

## 6. Success Metrics

- A new developer can run the API locally with Docker in under 10 minutes.
- OpenAPI docs expose all supported endpoints and schemas.
- Core workflows are covered by integration tests: signup, login, account creation, deposit, withdrawal, transfer, transaction history, and duplicate idempotency key handling.
- CI runs build and tests successfully on pull requests.
- Terraform configuration clearly documents the intended AWS deployment architecture.
- Recruiter or interviewer can understand the system purpose, architecture, API surface, and deployment plan from the repository documentation.

## 7. MVP Scope

### Included

- User registration and login.
- JWT access token authentication.
- Account creation and account lookup.
- Deposit funds into an account.
- Withdraw funds from an account.
- Transfer funds between accounts.
- Transaction history by account.
- Idempotency key support for mutation endpoints.
- Bean Validation for request payloads.
- Centralized exception handling and consistent error responses.
- PostgreSQL persistence.
- Docker Compose for local database and API runtime.
- OpenAPI/Swagger documentation.
- Integration tests with a test database.
- GitHub Actions CI pipeline.
- Terraform plan for AWS VPC, ECS Fargate, RDS PostgreSQL, CloudWatch logs, S3 artifacts, and environment variables.

### Deferred

- Account statements as PDFs.
- Admin dashboard.
- Rate limiting.
- Refresh tokens.
- Password reset flow.
- Role-based back-office operations.
- Production secrets management with AWS Secrets Manager.
- Blue/green deployment strategy.
- Event-driven ledger replication.

## 8. User Stories

### Authentication

- As a new user, I want to register with my personal details and credentials so that I can access CloudBank.
- As a registered user, I want to log in and receive a JWT so that I can call protected endpoints.
- As an authenticated user, I want my requests to be scoped to my own accounts so that other users cannot access my data.

### Accounts

- As an authenticated user, I want to create a bank account so that I can hold funds.
- As an authenticated user, I want to view my accounts so that I can see balances and account metadata.

### Transactions

- As an authenticated user, I want to deposit funds so that my account balance increases.
- As an authenticated user, I want to withdraw funds so that my account balance decreases when sufficient funds exist.
- As an authenticated user, I want to transfer funds to another account so that money moves atomically between accounts.
- As an authenticated user, I want to view transaction history so that I can audit account activity.

### Reliability and Auditability

- As an API client, I want to provide an idempotency key for money movement requests so that retries do not create duplicate transactions.
- As a reviewer, I want consistent validation and error responses so that API behavior is easy to understand.
- As a developer, I want automated tests and CI so that regressions are caught before merge.

## 9. Functional Requirements

### 9.1 User Management

- The system shall allow users to register with required identity and credential fields.
- The system shall store passwords using a secure one-way hash.
- The system shall prevent duplicate user registration by unique email.
- The system shall authenticate users and issue signed JWT access tokens.
- The system shall reject unauthenticated requests to protected endpoints.

### 9.2 Account Management

- The system shall allow authenticated users to create accounts.
- The system shall generate unique account identifiers.
- The system shall store account balance, status, owner, and timestamps.
- The system shall allow users to retrieve only their own accounts.
- The system shall prevent direct balance mutation except through transaction workflows.

### 9.3 Deposits

- The system shall allow deposits into an authenticated user's account.
- The system shall validate that deposit amounts are positive.
- The system shall update account balance and create a transaction record atomically.
- The system shall support idempotency keys to prevent duplicate deposit processing.

### 9.4 Withdrawals

- The system shall allow withdrawals from an authenticated user's account.
- The system shall validate that withdrawal amounts are positive.
- The system shall reject withdrawals when available balance is insufficient.
- The system shall update account balance and create a transaction record atomically.
- The system shall support idempotency keys to prevent duplicate withdrawal processing.

### 9.5 Transfers

- The system shall allow transfers from one account to another valid account.
- The system shall validate positive transfer amounts.
- The system shall reject transfers from accounts not owned by the authenticated user.
- The system shall reject transfers when the source account has insufficient funds.
- The system shall debit the source account, credit the destination account, and create transaction records atomically.
- The system shall support idempotency keys to prevent duplicate transfer processing.

### 9.6 Transaction History

- The system shall expose transaction history for an authenticated user's account.
- The system shall support sorting by transaction timestamp.
- The system should support pagination for transaction history.
- The system shall prevent users from viewing transactions for accounts they do not own.

### 9.7 Idempotency

- The system shall accept an idempotency key for all money movement endpoints.
- The system shall persist idempotency keys with request fingerprint, response status, and response body or resulting transaction reference.
- The system shall return the original result when the same key and same request are retried.
- The system shall reject reuse of the same key with a materially different request payload.
- The system shall enforce uniqueness of idempotency keys per user and endpoint context.

### 9.8 API Documentation

- The system shall expose OpenAPI documentation for all public endpoints.
- API docs shall include request schemas, response schemas, status codes, authentication requirements, and error examples.

## 10. API Surface

| Method | Endpoint | Description | Auth Required |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Register a user | No |
| POST | `/api/v1/auth/login` | Authenticate and issue JWT | No |
| POST | `/api/v1/accounts` | Create account | Yes |
| GET | `/api/v1/accounts` | List current user's accounts | Yes |
| GET | `/api/v1/accounts/{accountId}` | Get account details | Yes |
| POST | `/api/v1/accounts/{accountId}/deposits` | Deposit funds | Yes |
| POST | `/api/v1/accounts/{accountId}/withdrawals` | Withdraw funds | Yes |
| POST | `/api/v1/transfers` | Transfer funds between accounts | Yes |
| GET | `/api/v1/accounts/{accountId}/transactions` | Get transaction history | Yes |

## 11. Data Model

### User

- `id`
- `email`
- `passwordHash`
- `firstName`
- `lastName`
- `createdAt`
- `updatedAt`

### Account

- `id`
- `userId`
- `accountNumber`
- `balance`
- `currency`
- `status`
- `createdAt`
- `updatedAt`

### Transaction

- `id`
- `accountId`
- `type`
- `amount`
- `currency`
- `balanceAfter`
- `relatedAccountId`
- `transferId`
- `description`
- `createdAt`

### Transfer

- `id`
- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `status`
- `createdAt`

### Idempotency Record

- `id`
- `userId`
- `idempotencyKey`
- `endpoint`
- `requestHash`
- `responseStatus`
- `responseBody`
- `createdAt`

## 12. Non-Functional Requirements

### Security

- Use Spring Security for authentication and request filtering.
- Use JWT signing with environment-specific secrets.
- Hash passwords with a secure password encoder.
- Validate all request payloads with Bean Validation.
- Avoid exposing sensitive fields in API responses.
- Enforce account ownership checks for protected resources.

### Reliability

- Money movement operations must execute inside database transactions.
- Balance updates must avoid race conditions under concurrent requests.
- Duplicate retries must be handled through idempotency records.
- Error responses must be consistent and machine-readable.

### Performance

- API responses should complete within 300 ms locally for standard CRUD operations under normal development load.
- Transaction history should support pagination to avoid unbounded responses.

### Observability

- Application logs should include request correlation identifiers.
- Cloud deployment should publish logs to CloudWatch.
- Errors should be logged with enough context for debugging without leaking secrets.

### Maintainability

- Code should follow layered Spring Boot architecture: controller, service, repository, domain/entity, DTO, configuration, and exception packages.
- Business rules should live in service-layer logic, not controllers.
- Tests should cover both success paths and failure paths.

## 13. Error Handling

The API shall return consistent error responses with:

- HTTP status code.
- Stable error code.
- Human-readable message.
- Field-level validation errors when applicable.
- Request timestamp.
- Request path.

Representative errors:

- `400 Bad Request` for validation failures.
- `401 Unauthorized` for missing or invalid JWTs.
- `403 Forbidden` for ownership violations.
- `404 Not Found` for missing resources.
- `409 Conflict` for duplicate idempotency key misuse or duplicate user registration.
- `422 Unprocessable Entity` for insufficient funds.

## 14. Testing Requirements

- Unit tests for service-layer business rules.
- Integration tests for REST endpoints.
- Authentication tests for protected endpoints.
- Transactional tests for deposits, withdrawals, and transfers.
- Idempotency tests for duplicate and conflicting retries.
- Repository tests for PostgreSQL persistence where valuable.
- CI must run build and tests on pull requests and pushes to the main branch.

## 15. Deployment Requirements

### Local Development

- Docker Compose shall run PostgreSQL and the Spring Boot API.
- Local environment variables shall configure database URL, username, password, JWT secret, and active profile.

### AWS Deployment Scope

Terraform shall define:

- VPC and networking resources.
- ECS Fargate service for the API container.
- RDS PostgreSQL database.
- CloudWatch log groups.
- S3 bucket for deployment artifacts or build assets.
- Environment-specific variables.
- Security groups restricting database access to the API service.

## 16. Acceptance Criteria

- A user can register, log in, and call protected endpoints with a JWT.
- A user can create and retrieve only their own accounts.
- A deposit creates a transaction record and increases account balance.
- A withdrawal creates a transaction record and decreases account balance when funds are sufficient.
- A withdrawal fails cleanly when funds are insufficient.
- A transfer atomically debits one account and credits another.
- A repeated request with the same idempotency key returns the original result without duplicating money movement.
- A repeated request with the same idempotency key and different payload is rejected.
- Transaction history returns account activity in timestamp order.
- OpenAPI docs are available and accurate.
- Integration tests validate the core flows.
- GitHub Actions CI passes for build and test jobs.
- Terraform files clearly describe the AWS deployment architecture.

## 17. Risks and Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Race conditions during concurrent transfers | Incorrect balances | Use database transactions and row-level locking or optimistic locking. |
| Duplicate deposits or transfers from client retries | Financial inconsistency | Require idempotency keys for money movement endpoints. |
| JWT misconfiguration | Unauthorized access | Use tested Spring Security configuration and environment-specific secrets. |
| Overly broad portfolio scope | Incomplete project | Keep MVP focused on core banking workflows and defer advanced features. |
| Cloud cost exposure | Unexpected AWS spend | Document teardown steps and use minimal ECS/RDS sizing for practice. |
| Secrets committed to repository | Security exposure | Use environment variables and `.gitignore` for local secret files. |

## 18. Milestones

### Milestone 1: Foundation

- Initialize Spring Boot project.
- Configure PostgreSQL, profiles, and Docker Compose.
- Add base domain model and repository layer.

### Milestone 2: Authentication and Accounts

- Implement registration and login.
- Configure Spring Security and JWT filters.
- Implement account creation and retrieval.

### Milestone 3: Money Movement

- Implement deposits, withdrawals, and transfers.
- Add transaction history.
- Add idempotency key handling.

### Milestone 4: Quality and Documentation

- Add centralized exception handling.
- Add Bean Validation.
- Add OpenAPI docs.
- Add integration tests.

### Milestone 5: CI and Cloud Scope

- Add GitHub Actions CI.
- Add Docker image build configuration.
- Add Terraform AWS infrastructure scope.
- Document local and cloud setup steps.

## 19. Open Questions

- Should the MVP use a single default currency, such as USD, or make currency configurable at account creation?
- Should account numbers be human-readable generated identifiers or UUID-only identifiers?
- Should transfers between two accounts owned by the same user be treated differently from transfers to another user's account?
- Should idempotency keys expire after a retention period?
- Should the project include a Postman collection in addition to OpenAPI docs?

## 20. Recommended MVP Decision

Build the first version with a single currency, user-owned accounts, JWT authentication, required idempotency keys for money movement, PostgreSQL-backed persistence, and integration tests. Keep AWS deployment as Terraform-managed infrastructure scope with clear documentation, then expand only after the core API is reliable, testable, and easy for reviewers to run.
