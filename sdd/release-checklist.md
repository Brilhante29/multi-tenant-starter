# Release Checklist

## Pre-release

- [x] Unit tests pass in the Docker build: `./gradlew test`
- [x] PostgreSQL tests pass: `docker compose --profile test run --rm test`
- [x] Docker build: `docker build -t multi-tenant-starter .`
- [x] Docker runtime: `docker compose up --build app` (Actuator health endpoint)
- [x] Docker benchmark: `./tools/run-benchmark.ps1`
- [x] Benchmark JSON produced in `benchmarks/results/`
- [x] README has project number, claim, and benchmark result
- [x] REFERENCES.md complete
- [x] SDD files complete
- [x] No secret or paid credential required
- [x] .gitignore covers build artifacts
- [x] CI workflow validates same checks

## Post-release

- [ ] Published to GitHub
- [ ] CI passes on main branch
