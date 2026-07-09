# #17 multi-tenant-starter

**Status:** scaffold

**Proves:** multi-tenant real.

**Benchmark target:** tenant_onboarding_seconds.

**Stack:** java21, spring-boot, postgresql, flyway, docker.

## Next milestone

Implement the smallest Docker-runnable version and produce the first JSON benchmark under enchmarks/results/.

## Run

`ash
docker build -t multi-tenant-starter .
docker run --rm multi-tenant-starter
`

## Benchmark

`ash
docker run --rm multi-tenant-starter benchmark
`

| Metric | Value | Unit |
|---|---:|---|
| tenant_onboarding_seconds | pending | pending |

## Architecture

Defined in sdd/spec.md before implementation.

## References

See REFERENCES.md.