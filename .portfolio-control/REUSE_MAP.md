# Reuse Map: #17 multi-tenant-starter

## Kit Inputs

| Concern | Source of truth | Project use |
|---|---|---|
| Agent flow | `.portfolio/decision-brain/agent-graph.yaml` | sequential architecture, stack, benchmark, review, release gates |
| Engineering principles | `.portfolio/decision-brain/engineering-principles.yaml` | inward dependencies and narrow ports |
| Stack decisions | `.portfolio/decision-brain/stack-matrix.yaml` | Java/Spring/PostgreSQL local-first profile |
| Benchmark contract | `.portfolio/contracts/benchmark-result-v2.schema.json` | provenance and repeatable metrics envelope |
| SDD | repository `sdd/` | problem, architecture, technical, benchmark, handoff, reuse review |

## Project Delta

| Delta | Classification | Action |
|---|---|---|
| Schema-per-tenant SQL and provisioning adapters | project-specific | keep here |
| Source/image/lock/artifact benchmark provenance | reusable | recorded for kit harness backlog |
| Compose-hosted PostgreSQL integration tests without nested Docker | reusable | recorded for Java profile backlog |

No project implementation was copied into the reuse kit during this task because work was explicitly limited to this repository.
