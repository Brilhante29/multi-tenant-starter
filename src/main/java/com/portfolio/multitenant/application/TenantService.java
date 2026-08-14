package com.portfolio.multitenant.application;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.Tenant.Status;
import com.portfolio.multitenant.domain.TenantIdGenerator;
import com.portfolio.multitenant.domain.TenantRepository;
import com.portfolio.multitenant.domain.TenantSchemaManager;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TenantService {

    private final TenantRepository tenants;
    private final TenantSchemaManager schemas;
    private final TenantIdGenerator ids;
    private final TransactionRunner transactions;
    private final Clock clock;

    public TenantService(
        TenantRepository tenants,
        TenantSchemaManager schemas,
        TenantIdGenerator ids,
        TransactionRunner transactions,
        Clock clock
    ) {
        this.tenants = tenants;
        this.schemas = schemas;
        this.ids = ids;
        this.transactions = transactions;
        this.clock = clock;
    }

    public Tenant createTenant(String rawName) {
        String name = requireName(rawName);
        String id = requireUuid(ids.nextId());
        String schemaName = schemaNameFor(id);
        Instant createdAt = clock.instant();
        Tenant provisioning = new Tenant(id, name, schemaName, Status.PROVISIONING, createdAt, null);

        return transactions.required(() -> {
            tenants.insert(provisioning);
            schemas.provision(provisioning);
            return tenants.activate(id, clock.instant());
        });
    }

    public Tenant getTenant(String tenantId) {
        return tenants.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown tenant: " + tenantId));
    }

    public List<Tenant> listTenants() {
        return tenants.findAll();
    }

    public void deleteTenant(String tenantId) {
        transactions.required(() -> {
            Tenant tenant = getTenant(tenantId);
            schemas.drop(tenant.getSchemaName());
            tenants.deleteById(tenantId);
            return null;
        });
    }

    static String schemaNameFor(String tenantId) {
        return "tenant_" + UUID.fromString(tenantId).toString().replace("-", "");
    }

    private static String requireName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        String name = rawName.trim();
        if (name.length() > 120) {
            throw new IllegalArgumentException("name must be at most 120 characters");
        }
        return name;
    }

    private static String requireUuid(String value) {
        return UUID.fromString(value).toString();
    }
}
