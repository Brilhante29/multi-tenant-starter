package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantContext;
import com.portfolio.multitenant.domain.TenantRecord;
import com.portfolio.multitenant.domain.TenantRecordRepository;
import com.portfolio.multitenant.domain.TenantRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public final class JdbcTenantRecordRepository implements TenantRecordRepository {

    private final JdbcTemplate jdbc;
    private final TenantRepository tenants;

    public JdbcTenantRecordRepository(JdbcTemplate jdbc, TenantRepository tenants) {
        this.jdbc = jdbc;
        this.tenants = tenants;
    }

    @Override
    public TenantRecord insert(TenantRecord record) {
        String currentTenant = TenantContext.requireTenantId();
        if (!currentTenant.equals(record.tenantId())) {
            throw new IllegalArgumentException("Record owner does not match the active tenant");
        }
        String schema = activeSchema(currentTenant);
        jdbc.update(
            "INSERT INTO " + schema + ".tenant_records (id, tenant_id, payload, created_at) VALUES (?, ?, ?, ?)",
            UUID.fromString(record.id()),
            UUID.fromString(record.tenantId()),
            record.payload(),
            Timestamp.from(record.createdAt())
        );
        return record;
    }

    @Override
    public List<TenantRecord> findAllForCurrentTenant() {
        String tenantId = TenantContext.requireTenantId();
        String schema = activeSchema(tenantId);
        return jdbc.query(
            "SELECT id, tenant_id, payload, created_at FROM " + schema + ".tenant_records ORDER BY created_at, id",
            (result, rowNumber) -> new TenantRecord(
                result.getObject("id", UUID.class).toString(),
                result.getObject("tenant_id", UUID.class).toString(),
                result.getString("payload"),
                result.getTimestamp("created_at").toInstant()
            )
        );
    }

    private String activeSchema(String tenantId) {
        Tenant tenant = tenants.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown tenant: " + tenantId));
        if (tenant.getStatus() != Tenant.Status.ACTIVE) {
            throw new IllegalStateException("Tenant is not active: " + tenantId);
        }
        return PostgresSchemaNames.requireValid(tenant.getSchemaName());
    }
}
