# CloudBank Architecture Diagrams

Source inputs: technical architecture package.

## 1. System Context

```mermaid
flowchart LR
    User[Anonymous or Authenticated User]
    Reviewer[Developer / Reviewer]
    CI[GitHub Actions CI]
    AWS[AWS Runtime]

    API[CloudBank Spring Boot API]
    DB[(PostgreSQL)]
    Docs[OpenAPI / Swagger]
    CW[CloudWatch Logs]
    S3[S3 Artifacts]

    User -->|HTTPS REST /api/v1| API
    Reviewer -->|Local REST / Swagger| API
    Reviewer -->|Docker Compose| DB
    API -->|JPA / JDBC| DB
    API --> Docs
    CI -->|Build and tests| API
    AWS -->|ECS Fargate Task| API
    API -->|Container logs| CW
    AWS --> S3
```

## 2. Application Components

```mermaid
flowchart TB
    Client[HTTP Client]

    subgraph API[Spring Boot Application]
        Corr[Correlation ID Filter]
        Sec[JWT Security Filter]
        Controllers[REST Controllers]
        Validation[Bean Validation]
        Exceptions[Global Exception Handler]
        AuthSvc[Auth Service]
        AccountSvc[Account Service]
        MoneySvc[Money Movement Service]
        IdemSvc[Idempotency Service]
        TxnSvc[Transaction History Service]
        JwtSvc[JWT Service]
        Repos[Spring Data JPA Repositories]
        OpenAPI[OpenAPI Config]
    end

    DB[(PostgreSQL)]

    Client --> Corr --> Sec --> Controllers
    Controllers --> Validation
    Controllers --> AuthSvc
    Controllers --> AccountSvc
    Controllers --> MoneySvc
    Controllers --> TxnSvc
    MoneySvc --> IdemSvc
    AuthSvc --> JwtSvc
    AuthSvc --> Repos
    AccountSvc --> Repos
    MoneySvc --> Repos
    IdemSvc --> Repos
    TxnSvc --> Repos
    Repos --> DB
    Exceptions --> Controllers
    OpenAPI --> Controllers
```

## 3. Money Movement Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Security Filter
    participant API as Transaction Controller
    participant I as Idempotency Service
    participant M as Money Movement Service
    participant A as Account Repository
    participant T as Transaction Repository
    participant DB as PostgreSQL

    C->>S: JWT + Idempotency-Key + request body
    S->>API: Authenticated user context
    API->>I: Check key and request hash
    alt Existing matching idempotency record
        I-->>API: Stored response
        API-->>C: Replay original response
    else Existing conflicting idempotency record
        I-->>API: Conflict
        API-->>C: 409 Conflict
    else New request
        API->>M: Execute money movement
        M->>DB: Begin transaction
        M->>A: Lock affected account row(s)
        A->>DB: SELECT ... FOR UPDATE
        M->>A: Update balances
        M->>T: Insert transaction records
        M->>I: Persist idempotency response metadata
        M->>DB: Commit
        API-->>C: Operation response
    end
```

## 4. AWS Deployment Topology

```mermaid
flowchart TB
    Internet[Internet]
    ALB[Application Load Balancer]

    subgraph VPC[AWS VPC]
        subgraph Public[Public Subnets]
            ALB
        end
        subgraph Private[Private App Subnets]
            ECS[ECS Fargate Service]
        end
        subgraph Data[Private Data Subnets]
            RDS[(RDS PostgreSQL)]
        end
        CW[CloudWatch Logs]
        S3[S3 Artifact Bucket]
    end

    Internet -->|HTTPS| ALB
    ALB --> ECS
    ECS -->|JDBC private traffic| RDS
    ECS -->|logs| CW
    ECS -.optional artifacts.-> S3
```

## 5. Trust Boundaries

```mermaid
flowchart LR
    Untrusted[Untrusted Client Input]
    AuthBoundary[JWT Authentication Boundary]
    AppBoundary[Service Authorization Boundary]
    DataBoundary[Private Database Boundary]

    Untrusted -->|Validate DTOs and headers| AuthBoundary
    AuthBoundary -->|Authenticated principal| AppBoundary
    AppBoundary -->|Owned data only| DataBoundary
```
