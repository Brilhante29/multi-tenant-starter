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

SDD updated. PostgreSQL implementation, tests, benchmark evidence, README, and local commits are pending.
