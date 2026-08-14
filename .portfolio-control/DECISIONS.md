# Decision Register: #17 multi-tenant-starter

| Decision | Selected option | Evidence or reason | Revisit trigger |
|---|---|---|---|
| Architecture | Hexagonal single module | Keeps domain/use cases independent while exposing SQL and transaction boundaries | another bounded context appears |
| API style | REST | Narrow synchronous command/query surface | clients need graph-shaped aggregation |
| Messaging | none | No async delivery, replay, fan-out, or DLQ requirement | onboarding becomes asynchronous |
| Storage | PostgreSQL 17.6, schema per tenant | Proves physical namespaces, constraints, and transactional DDL | schema count or noisy-neighbor cost becomes material |
| Migration | Flyway for control; idempotent SQL per tenant | Stable shared migration plus transaction-bound tenant provisioning | online fleet migration becomes required |
| Local first | Docker Compose | Runs with no paid credential; managed PostgreSQL remains replaceable at datasource boundary | provider-specific behavior is required |
| ORM | rejected | JPA would hide schema-qualified SQL central to the proof | domain persistence becomes complex enough to justify it |

## Principles Check

- **SRP/ISP:** registry, schema lifecycle, records, transactions, and IDs are separate ports.
- **OCP/LSP:** test doubles and JDBC adapters preserve the same narrow contracts.
- **DIP:** application services import no Spring, JDBC, transport, broker, or cloud types.
- **DRY:** one versioned tenant migration provisions every schema.
- **KISS/YAGNI:** one database, no broker, ORM, distributed transaction, or cloud emulator.
