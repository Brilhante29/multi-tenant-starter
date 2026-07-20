# Spec: multi-tenant-starter

## Number

#17

## Claim

Este projeto prova que: multi-tenant real (schema-per-tenant isolation with ThreadLocal tenant context).

## Stack

java21, spring-boot, postgresql, flyway, docker

## User-visible output

- Docker command: `docker run --rm multi-tenant-starter`
- README opens with: `# #17 multi-tenant-starter`
- Benchmark table: tenant_onboarding_seconds

## Scope

In:

- Implementar o menor produto funcional que prove o claim.
- Rodar por Docker.
- Gerar benchmark JSON reproduzivel.

Out:

- Publicar repo antes do primeiro resultado numerico.
- Depender de segredo pago para o caminho default.

## Architecture

```
client -> HTTP -> TenantController -> TenantService -> domain ports -> in-memory adapters
```

Hexagonal architecture: domain defines ports, application orchestrates, infrastructure implements.

## Benchmark

Primary metric:

- name: tenant_onboarding_seconds
- target: first reproducible baseline
- command: `docker run --rm multi-tenant-starter benchmark`
- result file: `benchmarks/results/benchmark.json`

## Dataset or fixture

- source: generated (synthetic tenant names)
- size: 100 tenants default
- license: MIT
- deterministic seed: 42 (not applicable - sequential generation)

## Definition of done

- [x] Docker command works from clean clone.
- [x] README starts with project number and benchmark result.
- [x] Benchmark command writes JSON result.
- [x] Tests cover core behavior.
- [x] REFERENCES.md explains reuse.
- [x] No secret or paid credential required for default demo.
