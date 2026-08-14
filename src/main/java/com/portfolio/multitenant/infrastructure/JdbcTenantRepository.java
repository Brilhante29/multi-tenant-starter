package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JdbcTenantRepository implements TenantRepository {

    private static final RowMapper<Tenant> TENANT_ROW = (result, rowNumber) -> {
        Timestamp activatedAt = result.getTimestamp("activated_at");
        return new Tenant(
            result.getObject("id", UUID.class).toString(),
            result.getString("name"),
            result.getString("schema_name"),
            Tenant.Status.valueOf(result.getString("status")),
            result.getTimestamp("created_at").toInstant(),
            activatedAt == null ? null : activatedAt.toInstant()
        );
    };

    private final JdbcTemplate jdbc;

    public JdbcTenantRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Tenant insert(Tenant tenant) {
        jdbc.update(
            """
            INSERT INTO public.tenants (id, name, schema_name, status, created_at, activated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            UUID.fromString(tenant.getId()),
            tenant.getName(),
            PostgresSchemaNames.requireValid(tenant.getSchemaName()),
            tenant.getStatus().name(),
            Timestamp.from(tenant.getCreatedAt()),
            tenant.getActivatedAt() == null ? null : Timestamp.from(tenant.getActivatedAt())
        );
        return tenant;
    }

    @Override
    public Tenant activate(String tenantId, Instant activatedAt) {
        int updated = jdbc.update(
            """
            UPDATE public.tenants
               SET status = 'ACTIVE', activated_at = ?
             WHERE id = ? AND status = 'PROVISIONING'
            """,
            Timestamp.from(activatedAt),
            UUID.fromString(tenantId)
        );
        if (updated != 1) {
            throw new IllegalStateException("Tenant cannot be activated: " + tenantId);
        }
        return findById(tenantId).orElseThrow();
    }

    @Override
    public Optional<Tenant> findById(String tenantId) {
        List<Tenant> found = jdbc.query(
            """
            SELECT id, name, schema_name, status, created_at, activated_at
              FROM public.tenants
             WHERE id = ?
            """,
            TENANT_ROW,
            UUID.fromString(tenantId)
        );
        return found.stream().findFirst();
    }

    @Override
    public List<Tenant> findAll() {
        return jdbc.query(
            """
            SELECT id, name, schema_name, status, created_at, activated_at
              FROM public.tenants
             ORDER BY created_at, id
            """,
            TENANT_ROW
        );
    }

    @Override
    public void deleteById(String tenantId) {
        jdbc.update("DELETE FROM public.tenants WHERE id = ?", UUID.fromString(tenantId));
    }
}
