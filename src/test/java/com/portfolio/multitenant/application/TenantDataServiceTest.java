package com.portfolio.multitenant.application;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantContext;
import com.portfolio.multitenant.domain.TenantRecordRepository;
import com.portfolio.multitenant.domain.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantDataServiceTest {

    @AfterEach
    void clearContext() {
        TenantContext.clear();
    }

    @Test
    void clearsTenantContextWhenAnAdapterFails() {
        String id = "9b094a32-a049-4a04-a185-bb43a3fa11e4";
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRecordRepository records = mock(TenantRecordRepository.class);
        when(tenants.findById(id)).thenReturn(Optional.of(new Tenant(
            id,
            "Acme",
            "tenant_9b094a32a0494a04a185bb43a3fa11e4",
            Tenant.Status.ACTIVE,
            Instant.EPOCH,
            Instant.EPOCH
        )));
        when(records.findAllForCurrentTenant()).thenThrow(new RuntimeException("database unavailable"));
        TenantDataService service = new TenantDataService(
            tenants,
            records,
            () -> id,
            directTransactions(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
        );

        assertThrows(RuntimeException.class, () -> service.list(id));
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void rejectsNestedTenantScopes() {
        String id = "9b094a32-a049-4a04-a185-bb43a3fa11e4";
        TenantRepository tenants = mock(TenantRepository.class);
        when(tenants.findById(id)).thenReturn(Optional.of(new Tenant(
            id,
            "Acme",
            "tenant_9b094a32a0494a04a185bb43a3fa11e4",
            Tenant.Status.ACTIVE,
            Instant.EPOCH,
            Instant.EPOCH
        )));
        TenantDataService service = new TenantDataService(
            tenants,
            mock(TenantRecordRepository.class),
            () -> id,
            directTransactions(),
            Clock.systemUTC()
        );
        TenantContext.setTenantId("another-tenant");

        assertThrows(IllegalStateException.class, () -> service.list(id));
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
