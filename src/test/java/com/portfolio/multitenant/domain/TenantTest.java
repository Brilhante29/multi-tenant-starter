package com.portfolio.multitenant.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TenantTest {

    @Test
    void activationReturnsANewActiveTenant() {
        Instant created = Instant.parse("2026-08-14T00:00:00Z");
        Instant activated = created.plusSeconds(1);
        Tenant provisioning = new Tenant("id", "Acme", "tenant_schema", Tenant.Status.PROVISIONING, created, null);

        Tenant active = provisioning.activate(activated);

        assertEquals(Tenant.Status.PROVISIONING, provisioning.getStatus());
        assertEquals(Tenant.Status.ACTIVE, active.getStatus());
        assertEquals(activated, active.getActivatedAt());
    }

    @Test
    void equalityUsesOnlyTheStableId() {
        Tenant first = new Tenant("id", "A", "schema_a", Tenant.Status.PROVISIONING, Instant.EPOCH, null);
        Tenant same = new Tenant("id", "B", "schema_b", Tenant.Status.ACTIVE, Instant.EPOCH, Instant.EPOCH);
        Tenant other = new Tenant("other", "A", "schema_a", Tenant.Status.PROVISIONING, Instant.EPOCH, null);

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, other);
    }
}
