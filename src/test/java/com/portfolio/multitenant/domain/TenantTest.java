package com.portfolio.multitenant.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantTest {

    @Test
    void shouldCreateTenantWithAllFields() {
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Tenant tenant = new Tenant(id, "test-tenant", "schema_1234", Tenant.Status.ACTIVE, now);

        assertEquals(id, tenant.getId());
        assertEquals("test-tenant", tenant.getName());
        assertEquals("schema_1234", tenant.getSchema());
        assertEquals(Tenant.Status.ACTIVE, tenant.getStatus());
        assertEquals(now, tenant.getCreatedAt());
    }

    @Test
    void shouldTransitionThroughStatuses() {
        Tenant tenant = new Tenant();
        tenant.setId("1");
        tenant.setStatus(Tenant.Status.PROVISIONING);
        assertEquals(Tenant.Status.PROVISIONING, tenant.getStatus());

        tenant.setStatus(Tenant.Status.ACTIVE);
        assertEquals(Tenant.Status.ACTIVE, tenant.getStatus());

        tenant.setStatus(Tenant.Status.INACTIVE);
        assertEquals(Tenant.Status.INACTIVE, tenant.getStatus());
    }

    @Test
    void equalityShouldBeBasedOnId() {
        Tenant t1 = new Tenant("1", "a", "s1", Tenant.Status.ACTIVE, Instant.now());
        Tenant t2 = new Tenant("1", "b", "s2", Tenant.Status.INACTIVE, Instant.now());
        Tenant t3 = new Tenant("2", "a", "s1", Tenant.Status.ACTIVE, Instant.now());

        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void toStringShouldIncludeFields() {
        Tenant tenant = new Tenant("id1", "my-tenant", "schema_x", Tenant.Status.ACTIVE, Instant.now());
        String str = tenant.toString();
        assertTrue(str.contains("id1"));
        assertTrue(str.contains("my-tenant"));
        assertTrue(str.contains("schema_x"));
        assertTrue(str.contains("ACTIVE"));
    }
}
