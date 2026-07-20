package com.portfolio.multitenant.domain;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    Tenant save(Tenant tenant);
    Optional<Tenant> findById(String id);
    List<Tenant> findAll();
    void deleteById(String id);
}
