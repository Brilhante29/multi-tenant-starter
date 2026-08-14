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
| Benchmark V2 needs a standard provenance and comparability envelope | `patch_now` | `harness` | Reused the shared V2 envelope and separated canonical evidence from CI smoke output | resolved |
| Transactional schema onboarding is project-specific | `reject` | `component-packs` | Keep SQL and provisioning implementation here | rejected |
| Compose integration tests avoid nested-Docker assumptions | `backlog` | `language-profiles/java` | Add an external-PostgreSQL integration-test option | recorded |
| Validators should not require ripgrep or compile non-Python source as Python | `patch_now` | `validation` | Central kit validation now uses portable PowerShell discovery and language-aware checks | resolved |

## Final Gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects benchmark provenance and portable Compose execution mistakes discovered during the project.
