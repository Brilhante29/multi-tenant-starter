# References

| Reference | Used for | Copied code? |
|---|---|---|
| [PostgreSQL schemas](https://www.postgresql.org/docs/17/ddl-schemas.html) | Physical tenant namespace and qualified SQL | no |
| [PostgreSQL transactional DDL](https://wiki.postgresql.org/wiki/Transactional_DDL_in_PostgreSQL%3A_A_Competitive_Analysis) | Atomic registry plus schema onboarding | no |
| [Spring transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html) | Application-owned transaction boundary | no |
| [Spring JDBC](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html) | Explicit SQL adapters | no |
| [Flyway documentation](https://documentation.red-gate.com/fd) | Versioned control-plane migration | no |
| [Gradle dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html) | Reproducible dependency graph | no |
| [Docker Compose](https://docs.docker.com/compose/) | Local PostgreSQL runtime and integration tests | no |

All domain code, adapters, migrations, fixtures, tests, scripts, and benchmark evidence are project-specific implementations under MIT.
