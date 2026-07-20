# Architecture Decision

## Status

Accepted

## Context

Project: multi-tenant-starter
Claim: multi-tenant real (schema-per-tenant isolation)
Benchmark: tenant_onboarding_seconds

Problem forces:

- Domain complexity: low
- Integration pressure: low
- UI state complexity: none
- Data/ML reproducibility: low
- Auditability/event history: low
- Throughput/async pressure: low
- Independent deployability need: high

## Decision

Chosen architecture: hexagonal (ports/adapters)

Reason:

The multi-tenant pattern requires demonstrable tenant isolation. Hexagonal architecture isolates the domain (tenant entity, context, router) from transport (REST) and persistence (in-memory). This allows the schema-per-tenant proof to be tested without infrastructure dependencies, and makes the benchmark independent of database setup.

Dependency rule:

domain defines ports (interfaces). application depends on domain. infrastructure implements domain ports. benchmark exercises application through domain ports. No framework imports in domain layer.

## Rejected Alternatives

| Alternative | Why rejected |
|---|---|
| Layered architecture | Does not enforce port/adapter boundaries; tenant isolation pattern leaks into all layers. |
| Modular monolith | Over-engineered for a single-context multi-tenant proof. |

## Folder Layout

```
src/main/java/com/portfolio/multitenant/
  MultitenantApplication.java
  domain/      - Tenant, TenantContext, TenantRepository, DataSourceRouter
  application/ - TenantService, TenantController, HealthController
  infrastructure/ - InMemoryTenantRepository, InMemoryDataSourceRouter, SchemaInitializer, SimpleMigrationRunner
  benchmark/   - TenantBenchmark, BenchmarkResult
```

## Testing Strategy

- Unit tests: domain entities and TenantContext (no Spring context)
- Service tests: TenantService with Mockito (no infrastructure)
- Repository tests: InMemoryTenantRepository (direct instantiation)
- Benchmark test: TenantBenchmark with mocked Service (verifies result shape)

## Consequences

Positive:

- Tenant isolation is testable without databases or Docker Compose.
- In-memory adapters can be swapped for real PostgreSQL without changing domain or application code.
- Benchmark runs in a single JVM without external dependencies.

Tradeoffs:

- In-memory simulation does not prove PostgreSQL schema isolation.
- Each in-memory adapter must preserve the same contract as a future real adapter.

Migration path:

- Add `PostgresTenantRepository` and `PostgresDataSourceRouter` implementing the same ports.
- Add Flyway migrations for tenant schema creation.
- Use Testcontainers for integration tests.
