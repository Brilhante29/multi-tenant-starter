# Technical Decision

## Status

Accepted for implementation.

## Selected Stack

- Java 21 and Gradle wrapper with dependency locking.
- Spring Boot 3.4, Web, Actuator, JDBC, and transaction support.
- PostgreSQL 17.6 Alpine for the local-first runtime.
- Flyway for the shared control-plane schema.
- A versioned SQL migration executed on the transaction-bound JDBC connection for each tenant schema.
- Docker Compose for real-database integration tests without nested-Docker assumptions.

Java is retained. A Java-to-Kotlin rewrite would be cosmetic here and would expand the change surface without improving tenancy evidence.

## Database Decisions

The `public.tenants` registry has UUID, nonblank-name, unique-name, unique-schema, schema-pattern, status, and activation-state constraints. Tenant schemas contain their own migration history and `tenant_records` table. The tenant migration binds rows to the schema owner with a database CHECK constraint.

Flyway owns the stable shared schema. Tenant migrations run through Spring's transaction-bound connection because atomic registry-plus-DDL rollback is part of the proof.

## API and Messaging

REST is selected for synchronous onboarding and record inspection. GraphQL adds no caller benefit to this narrow command/query surface. Messaging is `none`: no asynchronous semantic, replay, routing, or DLQ requirement exists.

## Local First and Cloud

Docker Compose starts the application and PostgreSQL without credentials outside the local stack. Kumo is not used because the proof has no AWS-like behavior. A managed PostgreSQL provider can replace the datasource without entering domain or application code.

## Library Policy

Spring JDBC is selected over JPA so schema-qualified SQL and transactional DDL remain explicit. Flyway owns stable control migrations; a Compose test service provides PostgreSQL parity. No general-purpose abstraction is added until a second storage strategy needs it.

## Failure Semantics

- Any onboarding exception rolls back registry and schema creation.
- Invalid or inactive tenant access fails before tenant data SQL runs.
- Context is always cleared after scoped work.
- Constraint violations remain database errors and are mapped to HTTP conflict where appropriate.
