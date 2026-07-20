package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.domain.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTenantRepositoryTest {

    private InMemoryTenantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTenantRepository();
    }

    @Test
    void shouldSaveAndFindById() {
        Tenant tenant = createTenant("t1");
        Tenant saved = repository.save(tenant);

        assertEquals(tenant, saved);
        Optional<Tenant> found = repository.findById(tenant.getId());
        assertTrue(found.isPresent());
        assertEquals("t1", found.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Tenant> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void shouldUpdateExistingTenant() {
        Tenant tenant = createTenant("original");
        repository.save(tenant);

        tenant.setName("updated");
        repository.save(tenant);

        Tenant found = repository.findById(tenant.getId()).orElseThrow();
        assertEquals("updated", found.getName());
    }

    @Test
    void shouldFindAllTenants() {
        repository.save(createTenant("a"));
        repository.save(createTenant("b"));
        repository.save(createTenant("c"));

        List<Tenant> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    void shouldDeleteById() {
        Tenant tenant = createTenant("delete-me");
        repository.save(tenant);
        assertEquals(1, repository.findAll().size());

        repository.deleteById(tenant.getId());
        assertEquals(0, repository.findAll().size());
    }

    private Tenant createTenant(String name) {
        return new Tenant(UUID.randomUUID().toString(), name, "schema_" + name,
            Tenant.Status.ACTIVE, Instant.now());
    }
}
