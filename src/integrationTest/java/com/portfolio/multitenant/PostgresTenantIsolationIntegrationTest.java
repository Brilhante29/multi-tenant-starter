package com.portfolio.multitenant;

import com.portfolio.multitenant.application.TenantDataService;
import com.portfolio.multitenant.application.TenantService;
import com.portfolio.multitenant.application.TransactionRunner;
import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantRepository;
import com.portfolio.multitenant.domain.TenantSchemaManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PostgresTenantIsolationIntegrationTest {

    @Autowired
    TenantService tenants;
    @Autowired
    TenantDataService records;
    @Autowired
    TenantRepository tenantRepository;
    @Autowired
    TenantSchemaManager schemas;
    @Autowired
    TransactionRunner transactions;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        List<String> tenantSchemas = jdbc.queryForList(
            "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'tenant_%'",
            String.class
        );
        tenantSchemas.forEach(schemas::drop);
        jdbc.execute("TRUNCATE public.tenants");
    }

    @Test
    void isolatesQueriesAndRejectsAnOwnerFromAnotherTenant() {
        Tenant alpha = tenants.createTenant("alpha");
        Tenant beta = tenants.createTenant("beta");
        records.append(alpha.getId(), "alpha-only");
        records.append(beta.getId(), "beta-only");

        assertEquals(List.of("alpha-only"), records.list(alpha.getId()).stream().map(record -> record.payload()).toList());
        assertEquals(List.of("beta-only"), records.list(beta.getId()).stream().map(record -> record.payload()).toList());

        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
            "INSERT INTO " + alpha.getSchemaName()
                + ".tenant_records (id, tenant_id, payload, created_at) VALUES (?, ?, ?, ?)",
            UUID.randomUUID(),
            UUID.fromString(beta.getId()),
            "cross-tenant",
            java.sql.Timestamp.from(Instant.now())
        ));
    }

    @Test
    void rollsBackTheRegistryAndPhysicalSchemaWhenOnboardingFails() {
        String tenantId = "7da987a8-84e1-4168-8fd2-adf01f13efc2";
        String schemaName = "tenant_7da987a884e141688fd2adf01f13efc2";
        TenantSchemaManager failingAfterDdl = new TenantSchemaManager() {
            @Override
            public void provision(Tenant tenant) {
                schemas.provision(tenant);
                throw new IllegalStateException("injected failure after DDL");
            }

            @Override
            public boolean exists(String ignored) {
                return schemas.exists(ignored);
            }

            @Override
            public void drop(String ignored) {
                schemas.drop(ignored);
            }
        };
        TenantService failingService = new TenantService(
            tenantRepository,
            failingAfterDdl,
            () -> tenantId,
            transactions,
            Clock.fixed(Instant.parse("2026-08-14T00:00:00Z"), ZoneOffset.UTC)
        );

        assertThrows(IllegalStateException.class, () -> failingService.createTenant("rollback-proof"));
        assertTrue(tenantRepository.findById(tenantId).isEmpty());
        assertFalse(schemas.exists(schemaName));
    }

    @Test
    void reappliesTheTenantMigrationWithoutDuplicatingItsVersion() {
        Tenant tenant = tenants.createTenant("idempotent-migration");

        transactions.required(() -> {
            schemas.provision(tenant);
            return null;
        });

        Integer versions = jdbc.queryForObject(
            "SELECT count(*) FROM " + tenant.getSchemaName() + ".schema_history WHERE version = 1",
            Integer.class
        );
        assertEquals(1, versions);
    }

    @Test
    void onboardsUniqueIsolatedTenantsConcurrently() throws Exception {
        int concurrency = 8;
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(concurrency)) {
            List<Callable<Tenant>> tasks = java.util.stream.IntStream.range(0, concurrency)
                .mapToObj(index -> (Callable<Tenant>) () -> {
                    start.await();
                    Tenant tenant = tenants.createTenant("concurrent-" + index);
                    records.append(tenant.getId(), "owner-" + index);
                    return tenant;
                })
                .toList();
            List<Future<Tenant>> futures = tasks.stream().map(executor::submit).toList();
            start.countDown();
            List<Tenant> created = futures.stream().map(future -> {
                try {
                    return future.get();
                } catch (Exception exception) {
                    throw new AssertionError(exception);
                }
            }).toList();

            assertEquals(concurrency, new HashSet<>(created.stream().map(Tenant::getSchemaName).toList()).size());
            assertTrue(created.stream().allMatch(tenant -> tenant.getStatus() == Tenant.Status.ACTIVE));
            assertTrue(created.stream().allMatch(tenant -> records.list(tenant.getId()).size() == 1));
        }
    }
}
