# Benchmark Plan: multi-tenant-starter

## Hypothesis

multi-tenant real, measured by tenant_onboarding_seconds (time to create and provision a new tenant including schema generation, migration, and registration).

## Command

```bash
docker run --rm multi-tenant-starter benchmark
```

Optional argument: override tenant count `docker run --rm multi-tenant-starter benchmark 200`

## Environment

- OS: Linux (container), any host OS
- CPU: varies by host
- RAM: varies by host
- GPU: not applicable
- Docker version: any modern Docker
- Date: reported in benchmark JSON

## Inputs

- fixture: synthetic (sequential "benchmark-tenant-N" names)
- dataset size: 100 tenants (default)
- repetitions: 1 per run
- warmup: none (single shot)

## Metrics

| Metric | Unit | Source | Why it matters |
|---|---|---|---:|---|
| tenant_onboarding_seconds | seconds | TenantBenchmark (System.nanoTime) | proves the repo claim: schema-per-tenant isolation with reproducible throughput |

## Result schema

Output is JSON printed to stdout and written to `benchmarks/results/benchmark.json`:

```json
{
  "project": "multi-tenant-starter",
  "metric": "tenant_onboarding_seconds",
  "value": 0.015,
  "unit": "seconds",
  "iterations": 100,
  "timestamp": "2026-07-20T20:00:00Z",
  "environment": {
    "os": "Linux",
    "arch": "amd64",
    "cpus": 4
  },
  "command": "docker run --rm multi-tenant-starter benchmark"
}
```

## Post angle

#17 multi-tenant-starter: tenant_onboarding_seconds as a reproducible portfolio benchmark.
