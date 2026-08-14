# #17 multi-tenant-starter

**14.499 ms p50 onboarding, 0 tenant leaks, and 0 failures on PostgreSQL 17.6.**

This repository proves schema-per-tenant isolation with atomic onboarding, idempotent migrations, rollback after DDL failure, database ownership constraints, and concurrent provisioning. The benchmark used 2 warmup tenants and 3 measured repetitions of 6 tenants with 25 isolated queries per tenant.

## Benchmark

| Metric | Result | Evidence |
|---|---:|---|
| Tenant onboarding p50 | **14.499 ms/tenant** | median of 3 repetition p50 values |
| Tenant onboarding p95 | **17.541 ms/tenant** | median of 3 repetition p95 values |
| Isolated query p95 | **3.268 ms/query** | median of 3 repetition p95 values |
| Cross-tenant leakage | **0** | 450 measured queries |
| Failures | **0** | 3 repetitions |

Machine-readable result: [`benchmarks/results/multi-tenant-starter-v2.json`](benchmarks/results/multi-tenant-starter-v2.json). Results are local single-node evidence and do not represent managed PostgreSQL, WAN, hostile database roles, or thousands of schemas.

## Run

```bash
docker compose up --build app
```

The API is available at `http://localhost:8080`; health is at `GET /actuator/health`.

```bash
curl -X POST http://localhost:8080/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"name":"acme"}'
```

No paid credential or cloud account is required.

## Verify

PostgreSQL integration tests run entirely through Docker Compose:

```bash
docker compose --profile test run --rm test
```

Benchmark on Windows:

```powershell
./tools/run-benchmark.ps1
```

Benchmark on Linux/macOS:

```bash
./tools/run-benchmark.sh
```

Both benchmark commands build the current clean commit, capture image and dependency digests, execute at least three repetitions, write Benchmark Result V2 JSON, and reject nonzero leakage or failures.

## Architecture

```text
REST / benchmark
       |
TenantService + TenantDataService
       |
registry | schema lifecycle | records | transaction | id ports
       |
Spring JDBC + Flyway + PostgreSQL 17.6
```

- `public.tenants` is the control-plane catalog.
- Every tenant receives a generated `tenant_<uuid>` schema and versioned tables.
- Registry insert, schema DDL, migration, and activation share one PostgreSQL transaction.
- Tenant SQL uses only generated and regex-validated schema identifiers.
- A per-schema CHECK constraint rejects rows owned by another tenant.
- `TenantContext` is established and cleared around every data operation.

The domain and application layers import no Spring, JDBC, SQL, HTTP, Docker, or cloud SDK types. Messaging and cloud emulation are intentionally absent because onboarding and isolated queries are synchronous database concerns.

## Stack

Java 21, Spring Boot 3.4, Spring JDBC, Flyway 10, PostgreSQL 17.6, Gradle Wrapper with dependency locking, Docker Compose, JUnit 5, and GitHub Actions.

## References

See [`REFERENCES.md`](REFERENCES.md). Licensed under MIT.
