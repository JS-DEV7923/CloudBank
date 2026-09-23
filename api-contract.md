# CloudBank API Contract

Base path: `/api/v1`

Authentication: Bearer JWT for all endpoints except registration, login, and optional docs/health endpoints.

Money movement idempotency header: `Idempotency-Key`.

## 1. Common Error Response

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": [
    {
      "field": "amount",
      "message": "must be greater than 0"
    }
  ],
  "path": "/api/v1/accounts/{accountId}/deposits",
  "timestamp": "2026-09-23T00:00:00Z",
  "correlationId": "req-123"
}
```

## 2. Authentication

### Register

`POST /auth/register`

Request:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!",
  "firstName": "Jatin",
  "lastName": "Saini"
}
```

Success: `201 Created`

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "Jatin",
  "lastName": "Saini",
  "createdAt": "2026-09-23T00:00:00Z"
}
```

Errors:

- `400 VALIDATION_ERROR`.
- `409 EMAIL_ALREADY_EXISTS`.

### Login

`POST /auth/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!"
}
```

Success: `200 OK`

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "Jatin",
    "lastName": "Saini"
  }
}
```

Errors:

- `400 VALIDATION_ERROR`.
- `401 INVALID_CREDENTIALS`.

## 3. Accounts

### Create Account

`POST /accounts`

Auth: required.

Request:

```json
{}
```

Success: `201 Created`

```json
{
  "id": "uuid",
  "accountNumber": "1000000001",
  "balance": "0.00",
  "currency": "USD",
  "status": "ACTIVE",
  "createdAt": "2026-09-23T00:00:00Z"
}
```

### List Accounts

`GET /accounts`

Auth: required.

Success: `200 OK`

```json
[
  {
    "id": "uuid",
    "accountNumber": "1000000001",
    "balance": "125.00",
    "currency": "USD",
    "status": "ACTIVE",
    "createdAt": "2026-09-23T00:00:00Z"
  }
]
```

### Get Account

`GET /accounts/{accountId}`

Auth: required.

Success: `200 OK`

Errors:

- `404 ACCOUNT_NOT_FOUND` for missing or non-owned account by recommended convention.

## 4. Deposits

`POST /accounts/{accountId}/deposits`

Auth: required.

Headers:

- `Idempotency-Key: <client-generated-key>`

Request:

```json
{
  "amount": "100.00",
  "description": "Initial deposit"
}
```

Success: `200 OK`

```json
{
  "transactionId": "uuid",
  "accountId": "uuid",
  "type": "DEPOSIT",
  "amount": "100.00",
  "currency": "USD",
  "balanceAfter": "100.00",
  "createdAt": "2026-09-23T00:00:00Z"
}
```

Errors:

- `400 VALIDATION_ERROR`.
- `400 IDEMPOTENCY_KEY_REQUIRED`.
- `404 ACCOUNT_NOT_FOUND`.
- `409 IDEMPOTENCY_KEY_CONFLICT`.

## 5. Withdrawals

`POST /accounts/{accountId}/withdrawals`

Auth: required.

Headers:

- `Idempotency-Key: <client-generated-key>`

Request:

```json
{
  "amount": "50.00",
  "description": "ATM withdrawal"
}
```

Success: `200 OK`

```json
{
  "transactionId": "uuid",
  "accountId": "uuid",
  "type": "WITHDRAWAL",
  "amount": "50.00",
  "currency": "USD",
  "balanceAfter": "50.00",
  "createdAt": "2026-09-23T00:00:00Z"
}
```

Errors:

- `400 VALIDATION_ERROR`.
- `400 IDEMPOTENCY_KEY_REQUIRED`.
- `404 ACCOUNT_NOT_FOUND`.
- `409 IDEMPOTENCY_KEY_CONFLICT`.
- `422 INSUFFICIENT_FUNDS`.

## 6. Transfers

`POST /transfers`

Auth: required.

Headers:

- `Idempotency-Key: <client-generated-key>`

Request:

```json
{
  "sourceAccountId": "uuid",
  "destinationAccountId": "uuid",
  "amount": "25.00",
  "description": "Shared bill"
}
```

Success: `200 OK`

```json
{
  "transferId": "uuid",
  "sourceAccountId": "uuid",
  "destinationAccountId": "uuid",
  "amount": "25.00",
  "currency": "USD",
  "status": "COMPLETED",
  "sourceBalanceAfter": "75.00",
  "createdAt": "2026-09-23T00:00:00Z"
}
```

Errors:

- `400 VALIDATION_ERROR`.
- `400 IDEMPOTENCY_KEY_REQUIRED`.
- `404 SOURCE_ACCOUNT_NOT_FOUND`.
- `404 DESTINATION_ACCOUNT_NOT_FOUND`.
- `409 IDEMPOTENCY_KEY_CONFLICT`.
- `422 INSUFFICIENT_FUNDS`.

## 7. Transaction History

`GET /accounts/{accountId}/transactions?page=0&size=20`

Auth: required.

Success: `200 OK`

```json
{
  "content": [
    {
      "id": "uuid",
      "accountId": "uuid",
      "type": "DEPOSIT",
      "amount": "100.00",
      "currency": "USD",
      "balanceAfter": "100.00",
      "relatedAccountId": null,
      "transferId": null,
      "description": "Initial deposit",
      "createdAt": "2026-09-23T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Errors:

- `400 VALIDATION_ERROR`.
- `404 ACCOUNT_NOT_FOUND`.

## 8. Compatibility and Versioning

- `/api/v1` is the stable MVP API prefix.
- Breaking response or request changes should use a future `/api/v2`.
- Additive fields are allowed if clients can ignore unknown fields.
- Error `code` values should remain stable for tests and clients.

## 9. Idempotency Contract

- Required for deposits, withdrawals, and transfers.
- Key scope: authenticated user, endpoint, and idempotency key.
- Same key and same canonical request hash returns original response.
- Same key and different canonical request hash returns `409`.
- MVP retention: no expiration.
