package com.portfolio.multitenant.application;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.Tenant.Status;
import com.portfolio.multitenant.domain.TenantRepository;
import com.portfolio.multitenant.infrastructure.SchemaInitializer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final SchemaInitializer schemaInitializer;

    public TenantService(TenantRepository tenantRepository, SchemaInitializer schemaInitializer) {
        this.tenantRepository = tenantRepository;
        this.schemaInitializer = schemaInitializer;
    }

    public Tenant createTenant(String name) {
        String id = UUID.randomUUID().toString();
        String schemaName = "tenant_" + id.substring(0, 8);

        Tenant tenant = new Tenant(id, name, schemaName, Status.PROVISIONING, Instant.now());
        tenant = tenantRepository.save(tenant);

        try {
            schemaInitializer.initialize(id);
            tenant.setStatus(Status.ACTIVE);
            tenant = tenantRepository.save(tenant);
        } catch (Exception e) {
            tenant.setStatus(Status.INACTIVE);
            tenantRepository.save(tenant);
            throw new RuntimeException("Failed to provision tenant: " + name, e);
        }

        return tenant;
    }

    public List<Tenant> listTenants() {
        return tenantRepository.findAll();
    }
}
