# #17 multi-tenant-starter

**Status:** benchmarked

**Proves:** multi-tenant real (schema-per-tenant isolation with ThreadLocal tenant context).

**Benchmark target:** tenant_onboarding_seconds.

**Stack:** java21, spring-boot, postgresql, flyway, docker.

## Run

```bash
docker build -t multi-tenant-starter .
docker run --rm multi-tenant-starter
```

Health endpoint: [http://localhost:8080/api/health](http://localhost:8080/api/health)

## Benchmark

```bash
docker run --rm multi-tenant-starter benchmark
```

| Metric | Value | Unit |
|---|---:|---:|---|
| tenant_onboarding_seconds | 0.000154 | seconds/tenant |

## Architecture

```
src/main/java/com/portfolio/multitenant/
  MultitenantApplication.java
  domain/
    Tenant.java                - Tenant entity: id, name, schema, status, createdAt
    TenantRepository.java      - Port for tenant CRUD
    DataSourceRouter.java      - Port for schema-per-tenant query routing
    TenantContext.java         - ThreadLocal holder for current tenant
  application/
    TenantController.java      - REST: create tenant, list tenants
    TenantService.java         - Create tenant: generate schema, run migration, register
    HealthController.java      - Health check per tenant
  infrastructure/
    InMemoryTenantRepository.java - In-memory tenant store
    InMemoryDataSourceRouter.java - In-memory schema-per-tenant isolation
    SchemaInitializer.java     - Creates schema and runs migrations
    SimpleMigrationRunner.java - Creates tables per tenant schema
  benchmark/
    TenantBenchmark.java       - Onboards N tenants, measures time per tenant
    BenchmarkResult.java       - JSON result record
```

### Dependency rule

`domain` defines ports (interfaces). `application` depends on domain. `infrastructure` implements domain ports. `benchmark` exercises application through domain ports.

### Multi-tenant strategy

- **Schema-per-tenant** simulated in-memory with `Map<String, Map<String, List<Map>>>`.
- **TenantContext** propagated via `ThreadLocal`.
- Each tenant has an isolated data map (simulated schema).
- **Migration** creates `users`, `orders`, and `products` tables per schema.

## References

See REFERENCES.md.

## License

MIT
