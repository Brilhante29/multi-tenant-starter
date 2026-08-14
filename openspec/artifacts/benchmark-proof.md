# Benchmark Proof

- Source: `08faedad0ac094302c645aeef4338541afebe88a`
- Runtime: Java 21.0.11 and PostgreSQL 17.6 in Docker
- Workload: 2 warmup tenants; 3 repetitions; 6 tenants and 25 isolated queries per tenant per repetition
- Onboarding p50: `14.498945 ms/tenant`
- Onboarding p95: `17.541314 ms/tenant`
- Isolated-query p95: `3.26786 ms/query`
- Leakage: `0`
- Failures: `0`
- Artifact: `benchmarks/results/multi-tenant-starter-v2.json`
