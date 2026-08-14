# Agent Handoff

## Project

#17 `multi-tenant-starter`, program `backend-reliability-platform`.

## Current Objective

Replace the in-memory prototype with real PostgreSQL schema-per-tenant onboarding and evidence. Work only in this repository; do not push.

## Non-Negotiable Invariants

1. Registry insert, physical schema migration, and activation are atomic.
2. A failed onboarding leaves no registry row and no schema.
3. Schema identifiers are derived from UUIDs and validated before SQL interpolation.
4. Tenant data access establishes and clears `TenantContext` for every operation.
5. Benchmark publication requires zero failures and zero leakage.

## Planned Verification

```text
./gradlew test
./gradlew integrationTest
./tools/run-benchmark.ps1
./tools/validate-project.ps1 -SkipDocker
git diff --check
```

## Handoff State

PostgreSQL implementation, unit tests, Compose integration tests, benchmark V2 evidence, README, and local release commits are complete. Publication was intentionally not performed.

## Verified Evidence

- 12 unit tests passed in the Java 21 Docker build.
- 4 PostgreSQL integration tests passed, including rollback, leakage constraint, idempotency, and 8-way concurrent onboarding.
- Benchmark source: `08faedad0ac094302c645aeef4338541afebe88a`.
- Benchmark: p50 `14.498945 ms/tenant`, p95 `17.541314 ms/tenant`, query p95 `3.26786 ms`, leakage `0`, failures `0`.
