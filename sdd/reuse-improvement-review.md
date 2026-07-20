# Reuse Improvement Review

Project: `17 - multi-tenant-starter`

## Review Points

- [x] after scaffold
- [x] after architecture decision
- [x] after first working slice
- [x] after benchmark result
- [x] before publication
- [ ] after CI failure, if applicable

## Findings

| Finding | Classification | Kit Area | Action | Status |
|---|---|---|---|---|---|
| In-memory adapter pattern is reusable for other portfolio projects that need infrastructure-free demos | `patch_now` | `templates` | Add in-memory adapter template to portfolio-reuse-kit templates | pending |
| TenantContext ThreadLocal is a cross-cutting concern; could be extracted to shared library | `backlog` | `component-packs` | Extract to shared component pack when third project needs it | backlog |
| BenchmarkResult JSON serialization is ad-hoc; could use a shared library | `reject` | `harness` | JSON is simple enough; Jackson would add unnecessary dependency for this scope | rejected |

## Patch Now Decisions

- In-memory adapter template: the pattern of port -> in-memory implementation -> test is consistent and could be templated for new projects.

## Backlog Decisions

- TenantContext shared component: extract when a third multi-tenant project appears in the portfolio.

## Rejected Improvements

- BenchmarkResult JSON library: the manual serialization is ~20 lines and has zero dependencies. A shared library would add complexity without benefit for this scope.

## Final Gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects any repeated mistake discovered during the project.
