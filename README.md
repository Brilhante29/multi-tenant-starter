# Multi-Tenant Starter: Schema-per-Tenant Isolation on PostgreSQL

**14.499 ms p50 onboarding, 0 tenant leaks, and 0 failures on PostgreSQL 17.6.** Each tenant gets its own schema, created atomically with its registry entry and migrations, and 450 measured cross-checked queries found no data crossing tenant boundaries.

[![CI](https://github.com/Brilhante29/multi-tenant-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/Brilhante29/multi-tenant-starter/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)

## Why this exists

In a SaaS product, the worst bug is not downtime: it is one customer seeing another customer's data. Multi-tenancy also fails during onboarding, when a half-created tenant leaves an orphan schema or a registry entry pointing at nothing. This starter treats isolation and onboarding as invariants to prove, not conventions to remember:

- every tenant receives a generated `tenant_<uuid>` schema with versioned tables;
- registry insert, schema DDL, migration, and activation share **one PostgreSQL transaction**, so a DDL failure rolls everything back;
- tenant SQL only ever uses generated, regex-validated schema identifiers;
- a per-schema `CHECK` constraint rejects rows owned by another tenant, as a last line of defense below the application;
- the tenant context is set and cleared around every data operation.

## Results

| Metric | Result | Evidence |
|---|---:|---|
| Tenant onboarding p50 | **14.499 ms/tenant** | Median of 3 repetition p50 values |
| Tenant onboarding p95 | **17.541 ms/tenant** | Median of 3 repetition p95 values |
| Isolated query p95 | **3.268 ms/query** | Median of 3 repetition p95 values |
| Cross-tenant leakage | **0** | 450 measured queries |
| Failures | **0** | 3 repetitions |

The benchmark used 2 warm-up tenants and 3 measured repetitions of 6 tenants with 25 isolated queries per tenant, and it rejects any nonzero leakage or failure. Machine-readable result: [`benchmarks/results/multi-tenant-starter-v2.json`](benchmarks/results/multi-tenant-starter-v2.json).

## Quickstart

```bash
docker compose up --build app
curl -X POST http://localhost:8080/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"name":"acme"}'
```

Health: `GET /actuator/health`. No paid credential or cloud account is required.

Integration tests (PostgreSQL through Docker Compose):

```bash
docker compose --profile test run --rm test
```

Benchmark: `./tools/run-benchmark.sh` (or `tools/run-benchmark.ps1`). It builds the current clean commit, captures image and dependency digests, runs at least three repetitions, and writes Benchmark Result V2 JSON.

## How it works

```text
REST / benchmark
       |
TenantService + TenantDataService
       |
registry | schema lifecycle | records | transaction | id ports
       |
Spring JDBC + Flyway + PostgreSQL 17.6
```

`public.tenants` is the control-plane catalog. The domain and application layers import no Spring, JDBC, SQL, HTTP, Docker, or cloud SDK types; the JDBC and Flyway adapters implement their ports.

## Design decisions

| Decision | Why | Rejected |
|---|---|---|
| Schema per tenant | Strong isolation with per-tenant migrations, without one database per customer | Shared tables with a `tenant_id` column only; database per tenant |
| One transaction for onboarding | No orphan schemas or dangling registry rows after failures | Multi-step provisioning without rollback |
| Database `CHECK` ownership constraint | Defense in depth if application code ever slips | Trusting the application layer alone |
| Generated, validated identifiers | Schema names never come from user input | Building SQL from tenant names |
| Synchronous, no broker or cloud | Onboarding and isolated queries are database concerns | Infrastructure that does not serve the claim |

## Limitations

- Local single-node evidence; not managed PostgreSQL, WAN latency, or thousands of schemas.
- Database roles are not hostile-tenant hardened (no per-tenant role or row-level security yet).
- Catalog growth and migration fan-out at large tenant counts are not measured.

## Stack

Java 21, Spring Boot 3.4, Spring JDBC, Flyway 10, PostgreSQL 17.6, Gradle Wrapper with dependency locking, Docker Compose, JUnit 5, and GitHub Actions.

## Project structure

```text
src/main/java/com/portfolio/multitenant/
  domain/           tenant model and invariants
  application/      services and ports
  infrastructure/   JDBC registry, schema lifecycle, records, transactions
  web/              REST adapter
  benchmark/        onboarding and isolation workload
src/main/resources/db/   control-plane and tenant migrations
benchmarks/  tools/      results, benchmark runners, validators
sdd/  openspec/          decisions, benchmark plan, handoff
```

## How this repository is built

The project follows the spec-driven workflow of [portfolio-reuse-kit](https://github.com/Brilhante29/portfolio-reuse-kit). Requirements and decisions live in [`sdd/`](sdd) and [`openspec/`](openspec), and [`project.yaml`](project.yaml) records the architecture, stack, and rejected alternatives. Development is AI-assisted and human-governed: [`AGENTS.md`](AGENTS.md) and [`CLAUDE.md`](CLAUDE.md) hold the coding-agent instructions, while tests, validators, and CI decide what gets published.

## Related work

- [spring-hexagonal-payments](https://github.com/Brilhante29/spring-hexagonal-payments): database-enforced invariants for payment idempotency.
- [api-gateway-lite](https://github.com/Brilhante29/api-gateway-lite): authentication and per-key quotas in front of services like this one.

See [`REFERENCES.md`](REFERENCES.md) for attribution.

## Author

**Guilherme Brilhante**, software engineer working on scalable backends and production AI.
[LinkedIn](https://www.linkedin.com/in/guilhermefreirebrilhanteseveriano/) · [GitHub](https://github.com/Brilhante29) · [Publications](https://dblp.org/pid/353/6812.html)

## License

[MIT](LICENSE).
