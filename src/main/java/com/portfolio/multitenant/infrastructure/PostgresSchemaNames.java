package com.portfolio.multitenant.infrastructure;

import java.util.regex.Pattern;

final class PostgresSchemaNames {

    private static final Pattern TENANT_SCHEMA = Pattern.compile("tenant_[0-9a-f]{32}");

    private PostgresSchemaNames() {}

    static String requireValid(String schemaName) {
        if (schemaName == null || !TENANT_SCHEMA.matcher(schemaName).matches()) {
            throw new IllegalArgumentException("Invalid tenant schema name: " + schemaName);
        }
        return schemaName;
    }
}
