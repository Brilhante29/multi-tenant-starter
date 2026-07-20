package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.domain.DataSourceRouter;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer {

    private final DataSourceRouter router;
    private final SimpleMigrationRunner migrationRunner;

    public SchemaInitializer(DataSourceRouter router, SimpleMigrationRunner migrationRunner) {
        this.router = router;
        this.migrationRunner = migrationRunner;
    }

    public void initialize(String tenantId) {
        router.createSchema(tenantId);
        migrationRunner.runMigrations(tenantId);
    }
}
