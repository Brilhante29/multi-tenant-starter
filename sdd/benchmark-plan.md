# Benchmark Plan V2

## Hypothesis

PostgreSQL schema-per-tenant onboarding remains measurable while isolated queries produce zero cross-tenant leakage under repeated work.

## Command

Windows:

```powershell
./tools/run-benchmark.ps1
```

Linux/macOS/CI:

```bash
./tools/run-benchmark.sh
```

Both commands build the exact source commit, start PostgreSQL with Docker Compose, run the workload, write `benchmarks/results/benchmark.json`, validate integrity, and remove the volume.

## Fixed Workload

- warmup tenants: 2
- measured tenants per repetition: 6
- isolated queries per tenant: 25
- repetitions: 3 minimum
- fixture: deterministic names and one owner-marked record per tenant

## Metrics

| Metric | Unit | Direction |
|---|---|---|
| `tenant_onboarding_p50_ms` | milliseconds/tenant | lower |
| `tenant_onboarding_p95_ms` | milliseconds/tenant | lower |
| `isolated_query_p50_ms` | milliseconds/query | lower |
| `isolated_query_p95_ms` | milliseconds/query | lower |
| `failures_count` | count | must be 0 |
| `leakage_count` | count | must be 0 |

## Reproducibility Contract

The JSON records workload shape, warmup, repetitions, all samples, PostgreSQL version, JVM/OS/CPU, source commit, application image ID, PostgreSQL image ID, Gradle lock SHA-256, command, and a comparability key. Results are comparable only when the comparability key matches.

## Limits

This is a local single-node benchmark. It does not model connection-pool saturation, noisy neighbors, schema counts in the thousands, WAN latency, managed-service behavior, or hostile database roles.
