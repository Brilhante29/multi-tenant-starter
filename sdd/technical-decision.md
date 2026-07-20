# Technical Decision

## Status

Accepted

## Decision Type

stack, api-style, database, library, runtime

## Context

Project: multi-tenant-starter
Problem: Prove schema-per-tenant isolation with a reproducible benchmark.
Portfolio program: backend-reliability-platform
Public signal: Java + Spring multi-tenant with architecture boundaries, tests, and benchmark evidence.
Benchmark: tenant_onboarding_seconds

## Selected Option

Selected: Java 21 + Spring Boot 3.4 + in-memory schema-per-tenant

Reason:

Java + Spring Boot is the idiomatic enterprise choice for multi-tenant SaaS patterns. Schema-per-tenant is the strongest isolation model. In-memory simulation proves the pattern without requiring PostgreSQL infrastructure for the default demo. The benchmark measures tenant creation throughput (schema generation + migration + registration).

## Decision Brain Fields

- Stack profile: spring-kotlin-backend
- API style: rest-http
- Messaging: none
- Cloud mode: adapter-fake
- Database/runtime: in-memory (simulates schema-per-tenant), Docker runtime
- Library policy: Minimal dependencies - Spring Boot Web, Actuator, Test. No JPA or Flyway for local path.

## Engineering Principles

Coupling boundary:

Domain/use cases must not depend on framework, DB, broker, cloud SDK, transport, or UI.

SOLID application:

- SRP: Tenant owns identity/state; TenantRepository owns persistence contract; DataSourceRouter owns query routing.
- OCP: New repository implementations (e.g., PostgreSQL) extend without modifying domain.
- LSP: InMemoryTenantRepository and future PostgresTenantRepository share the same contract.
- ISP: TenantRepository (4 methods) and DataSourceRouter (4 methods) are minimal focused ports.
- DIP: TenantService depends on TenantRepository and SchemaInitializer abstractions.

Simplicity:

- KISS: In-memory ConcurrentHashMap for tenants + Map for schema data. No ORM, no connection pool.
- YAGNI: No JPA/Hibernate, no Flyway, no Docker Compose, no connection pooling for local demo.
- DRY: Migration runner is a single method; no duplicated SQL between tenants.

Testability evidence:

- TenantService test with Mockito: verifies tenant creation lifecycle without infrastructure.
- InMemoryTenantRepository test: verifies CRUD with plain JUnit.
- TenantContext test: verifies ThreadLocal isolation across threads.

## Rejected Options

| Option | Why rejected |
|---|---|
| PostgreSQL + Flyway for local demo | Adds operational overhead (Docker Compose, startup time) without changing the schema-per-tenant proof. |
| JPA/Hibernate | ORM complexity not needed for an in-memory proof; adds unnecessary coupling. |
| Testcontainers | Valid for real-database integration tests but over-engineering for the local-first benchmark path. |
| Multi-module Gradle | Single module is simpler and sufficient for a single-context proof. |

## API Contract

Contract artifact: Implicit REST contract (no OpenAPI spec for this scope)

## Cloud Local-First

Local provider: none (adapter fake)

Real provider target: none

Config switch: none

## Benchmark Impact

Expected impact: tenant_onboarding_seconds measures the throughput of in-memory tenant creation including schema simulation and migration.

Validation command:
```bash
docker build -t multi-tenant-starter .
docker run --rm multi-tenant-starter benchmark
```

## Operational Cost

- Docker services added: none (single JVM container)
- Local demo complexity: low
- Failure case required: yes (migration failure sets tenant to INACTIVE)

## Follow-up

- If benchmark shows unexpectedly high latency, review SimpleMigrationRunner for unnecessary synchronization.
- If real PostgreSQL integration is needed, add PostgresTenantRepository and Flyway.
