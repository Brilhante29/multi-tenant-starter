package com.portfolio.multitenant.application;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantContext;
import com.portfolio.multitenant.domain.TenantIdGenerator;
import com.portfolio.multitenant.domain.TenantRecord;
import com.portfolio.multitenant.domain.TenantRecordRepository;
import com.portfolio.multitenant.domain.TenantRepository;

import java.time.Clock;
import java.util.List;
import java.util.function.Supplier;

public final class TenantDataService {

    private final TenantRepository tenants;
    private final TenantRecordRepository records;
    private final TenantIdGenerator ids;
    private final TransactionRunner transactions;
    private final Clock clock;

    public TenantDataService(
        TenantRepository tenants,
        TenantRecordRepository records,
        TenantIdGenerator ids,
        TransactionRunner transactions,
        Clock clock
    ) {
        this.tenants = tenants;
        this.records = records;
        this.ids = ids;
        this.transactions = transactions;
        this.clock = clock;
    }

    public TenantRecord append(String tenantId, String payload) {
        requireActiveTenant(tenantId);
        return inTenantScope(tenantId, () -> transactions.required(() ->
            records.insert(new TenantRecord(ids.nextId(), tenantId, payload, clock.instant()))
        ));
    }

    public List<TenantRecord> list(String tenantId) {
        requireActiveTenant(tenantId);
        return inTenantScope(tenantId, records::findAllForCurrentTenant);
    }

    private Tenant requireActiveTenant(String tenantId) {
        Tenant tenant = tenants.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown tenant: " + tenantId));
        if (tenant.getStatus() != Tenant.Status.ACTIVE) {
            throw new IllegalStateException("Tenant is not active: " + tenantId);
        }
        return tenant;
    }

    private static <T> T inTenantScope(String tenantId, Supplier<T> work) {
        if (TenantContext.getTenantId() != null) {
            throw new IllegalStateException("Nested tenant context is not allowed");
        }
        TenantContext.setTenantId(tenantId);
        try {
            return work.get();
        } finally {
            TenantContext.clear();
        }
    }
}
