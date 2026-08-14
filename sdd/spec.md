# Spec: multi-tenant-starter

## Number and Claim

#17 proves real PostgreSQL schema-per-tenant isolation. Onboarding must create the registry row, physical schema, versioned tenant tables, constraints, and ACTIVE state in one transaction.

## Portfolio Fit

Program: `backend-reliability-platform`.

This repository is the tenancy boundary for the platform. It demonstrates that a SaaS control plane can provision isolated data planes without coupling domain or use-case code to PostgreSQL or Spring.

## In Scope

- Java 21, Spring Boot, Spring JDBC, PostgreSQL 17, Flyway, and Docker Compose.
- Physical schema per tenant with generated, validated SQL identifiers.
- Atomic onboarding and deletion with PostgreSQL transactional DDL.
- Tenant-owned row constraint inside every tenant schema.
- REST endpoints for tenant onboarding and isolated records.
- Unit tests plus PostgreSQL isolation, rollback, and concurrency tests.
- Benchmark V2 with warmup, at least three repetitions, integrity counters, and exact provenance.

## Out of Scope

- Cross-region tenancy, billing, authentication, row-level security, and online schema-upgrade orchestration.
- Hibernate/JPA, Kafka, RabbitMQ, Kubernetes, or paid cloud dependencies.
- Claiming that schema-per-tenant is sufficient for hostile-database-user isolation.

## Acceptance Criteria

- [ ] Default runtime uses PostgreSQL, never an in-memory schema simulation.
- [ ] Failed onboarding leaves neither registry row nor schema.
- [ ] Tenant A cannot read or write Tenant B records through the application path.
- [ ] A database constraint rejects a row whose owner differs from its schema tenant.
- [ ] Concurrent onboarding creates unique schemas and preserves isolation.
- [ ] Benchmark has at least three repetitions, warmup, zero failures, and zero leakage.
- [ ] Result records source commit, image IDs, dependency lock hash, PostgreSQL version, and comparability key.
- [ ] README opens with #17, measured result, and explicit limits.
