package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantSchemaManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class JdbcTenantSchemaManager implements TenantSchemaManager {

    private final JdbcTemplate jdbc;
    private final String migrationSql;

    public JdbcTenantSchemaManager(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.migrationSql = loadMigration();
    }

    @Override
    public void provision(Tenant tenant) {
        String schema = PostgresSchemaNames.requireValid(tenant.getSchemaName());
        String tenantId = UUID.fromString(tenant.getId()).toString();
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS " + schema);

        String resolved = migrationSql
            .replace("${schema}", schema)
            .replace("${tenant_id}", tenantId);
        for (String statement : resolved.split(";")) {
            if (!statement.isBlank()) {
                jdbc.execute(statement.trim());
            }
        }
    }

    @Override
    public boolean exists(String schemaName) {
        Integer count = jdbc.queryForObject(
            "SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
            Integer.class,
            PostgresSchemaNames.requireValid(schemaName)
        );
        return count != null && count == 1;
    }

    @Override
    public void drop(String schemaName) {
        jdbc.execute("DROP SCHEMA IF EXISTS " + PostgresSchemaNames.requireValid(schemaName) + " CASCADE");
    }

    private static String loadMigration() {
        try {
            return StreamUtils.copyToString(
                new ClassPathResource("db/tenant/V1__tenant_records.sql").getInputStream(),
                StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Tenant migration could not be loaded", exception);
        }
    }
}
