# Release Checklist

## Pre-release

- [x] Tests pass locally: `gradle test`
- [x] Docker build: `docker build -t multi-tenant-starter .`
- [x] Docker run: `docker run --rm multi-tenant-starter` (health endpoint)
- [x] Docker benchmark: `docker run --rm multi-tenant-starter benchmark`
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
