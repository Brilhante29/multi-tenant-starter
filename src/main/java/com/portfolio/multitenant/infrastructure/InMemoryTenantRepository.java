package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryTenantRepository implements TenantRepository {

    private final ConcurrentMap<String, Tenant> store = new ConcurrentHashMap<>();

    @Override
    public Tenant save(Tenant tenant) {
        store.put(tenant.getId(), tenant);
        return tenant;
    }

    @Override
    public Optional<Tenant> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Tenant> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
