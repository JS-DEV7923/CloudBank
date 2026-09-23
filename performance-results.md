# CloudBank Performance Results

## Requirement

REQ-028: Standard local CRUD operations should complete within 300 ms under normal development load.

## Result

Status: `NOT TESTED`.

`mvn verify` completed successfully, but no latency benchmark or timed API measurement was performed.

## Confidence Limits

- Integration tests exercise endpoints in-process with MockMvc and H2, which is useful for correctness but not representative of HTTP server, PostgreSQL, Docker, or AWS runtime latency.
- No throughput, concurrency, lock contention, database pool, or resource usage measurements were collected.

## Recommended Follow-Up

- Add a lightweight performance smoke test for auth/account/deposit/history under local profile.
- Add a concurrency test for competing withdrawals/transfers against PostgreSQL.
- Measure Docker Compose API latency against PostgreSQL before claiming the 300 ms target.
