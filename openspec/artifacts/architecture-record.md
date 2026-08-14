# Architecture Record

Accepted: a single-module hexagonal service with `public.tenants` as control plane and one PostgreSQL schema per tenant. Application services own transaction and tenant-scope policy; Spring JDBC adapters own SQL and validated schema interpolation.

Rejected: in-memory maps, JPA multi-tenancy, database per tenant, messaging, and cloud services. They either cannot prove the claim or add machinery unrelated to synchronous onboarding and isolated queries.
