package com.portfolio.multitenant.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @Test
    void shouldDefaultToNull() {
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void shouldSetAndGetTenantId() {
        TenantContext.setTenantId("tenant-1");
        assertEquals("tenant-1", TenantContext.getTenantId());
        TenantContext.clear();
    }

    @Test
    void shouldBeIsolatedBetweenThreads() throws InterruptedException {
        TenantContext.setTenantId("main-thread");

        Thread t = new Thread(() -> {
            assertNull(TenantContext.getTenantId());
            TenantContext.setTenantId("worker-thread");
            assertEquals("worker-thread", TenantContext.getTenantId());
            TenantContext.clear();
            assertNull(TenantContext.getTenantId());
        });

        t.start();
        t.join();

        assertEquals("main-thread", TenantContext.getTenantId());
        TenantContext.clear();
    }

    @Test
    void clearShouldRemoveValue() {
        TenantContext.setTenantId("something");
        assertEquals("something", TenantContext.getTenantId());
        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
    }
}
