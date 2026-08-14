package com.portfolio.multitenant.domain;

public interface TenantSchemaManager {
    void provision(Tenant tenant);

    boolean exists(String schemaName);

    void drop(String schemaName);
}
