package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.domain.DataSourceRouter;
import com.portfolio.multitenant.domain.TenantContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryDataSourceRouter implements DataSourceRouter {

    private final ConcurrentMap<String, Map<String, List<Map<String, Object>>>> schemas = new ConcurrentHashMap<>();

    @Override
    public Map<String, List<Map<String, Object>>> getCurrentSchema() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context set");
        }
        Map<String, List<Map<String, Object>>> schema = schemas.get(tenantId);
        if (schema == null) {
            throw new IllegalStateException("No schema found for tenant: " + tenantId);
        }
        return schema;
    }

    @Override
    public boolean hasSchema(String tenantId) {
        return schemas.containsKey(tenantId);
    }

    @Override
    public void createSchema(String tenantId) {
        schemas.put(tenantId, new ConcurrentHashMap<>());
    }

    @Override
    public void dropSchema(String tenantId) {
        schemas.remove(tenantId);
    }
}
