package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.domain.DataSourceRouter;
import com.portfolio.multitenant.domain.TenantContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SimpleMigrationRunner {

    private final DataSourceRouter router;

    public SimpleMigrationRunner(DataSourceRouter router) {
        this.router = router;
    }

    public void runMigrations(String tenantId) {
        TenantContext.setTenantId(tenantId);
        try {
            Map<String, List<Map<String, Object>>> schema = router.getCurrentSchema();
            schema.put("users", new CopyOnWriteArrayList<>());
            schema.put("orders", new CopyOnWriteArrayList<>());
            schema.put("products", new CopyOnWriteArrayList<>());
        } finally {
            TenantContext.clear();
        }
    }
}
