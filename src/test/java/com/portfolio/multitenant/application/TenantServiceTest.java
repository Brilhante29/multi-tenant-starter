package com.portfolio.multitenant.application;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantIdGenerator;
import com.portfolio.multitenant.domain.TenantRepository;
import com.portfolio.multitenant.domain.TenantSchemaManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    private static final String TENANT_ID = "9b094a32-a049-4a04-a185-bb43a3fa11e4";
    private static final Instant NOW = Instant.parse("2026-08-14T00:00:00Z");

    @Mock
    private TenantRepository tenants;
    @Mock
    private TenantSchemaManager schemas;

    private TenantService service;

    @BeforeEach
    void setUp() {
        TenantIdGenerator ids = () -> TENANT_ID;
        TransactionRunner transactions = directTransactions();
        service = new TenantService(
            tenants,
            schemas,
            ids,
            transactions,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void provisionsThenActivatesInsideTheTransactionBoundary() {
        String schema = "tenant_9b094a32a0494a04a185bb43a3fa11e4";
        Tenant active = new Tenant(TENANT_ID, "Acme", schema, Tenant.Status.ACTIVE, NOW, NOW);
        when(tenants.activate(TENANT_ID, NOW)).thenReturn(active);

        Tenant result = service.createTenant("  Acme  ");

        assertEquals(active, result);
        InOrder order = inOrder(tenants, schemas);
        order.verify(tenants).insert(any(Tenant.class));
        order.verify(schemas).provision(any(Tenant.class));
        order.verify(tenants).activate(TENANT_ID, NOW);
    }

    @Test
    void doesNotActivateWhenProvisioningFails() {
        RuntimeException failure = new RuntimeException("migration failed");
        org.mockito.Mockito.doThrow(failure).when(schemas).provision(any(Tenant.class));

        assertEquals(failure, assertThrows(RuntimeException.class, () -> service.createTenant("Acme")));
        verify(tenants, never()).activate(any(), any());
    }

    @Test
    void rejectsBlankAndOversizedNamesBeforePersistence() {
        assertThrows(IllegalArgumentException.class, () -> service.createTenant(" "));
        assertThrows(IllegalArgumentException.class, () -> service.createTenant("x".repeat(121)));
        verify(tenants, never()).insert(any());
    }

    private static TransactionRunner directTransactions() {
        return new TransactionRunner() {
            @Override
            public <T> T required(Supplier<T> work) {
                return work.get();
            }
        };
    }
}
