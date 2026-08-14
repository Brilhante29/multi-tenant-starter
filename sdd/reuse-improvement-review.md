# Reuse Improvement Review

Project: `17 - multi-tenant-starter`

## Review Points

- [x] after architecture decision
- [x] after PostgreSQL implementation
- [x] after integration tests
- [x] after benchmark result
- [x] before local release commit

## Findings

| Finding | Classification | Kit Area | Action | Status |
|---|---|---|---|---|
| Benchmark V2 needs a standard provenance and comparability envelope | `backlog` | `harness` | Reuse this repository's source/image/lock/artifact digest envelope | recorded |
| Transactional schema onboarding is project-specific | `reject` | `component-packs` | Keep SQL and provisioning implementation here | rejected |
| Compose integration tests avoid nested-Docker assumptions | `backlog` | `language-profiles/java` | Add an external-PostgreSQL integration-test option | recorded |
| Validators should not require ripgrep or compile non-Python source as Python | `backlog` | `validation` | Record a portable validation rule for the kit | recorded |

## Final Gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects benchmark provenance and portable Compose execution mistakes discovered during the project.
