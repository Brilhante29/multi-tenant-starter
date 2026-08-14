package com.portfolio.multitenant.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    Tenant insert(Tenant tenant);

    Tenant activate(String tenantId, Instant activatedAt);

    Optional<Tenant> findById(String tenantId);

    List<Tenant> findAll();

    void deleteById(String tenantId);
}
