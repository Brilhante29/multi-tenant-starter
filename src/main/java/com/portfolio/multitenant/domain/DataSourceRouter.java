package com.portfolio.multitenant.domain;

import java.util.List;
import java.util.Map;

public interface DataSourceRouter {
    Map<String, List<Map<String, Object>>> getCurrentSchema();
    boolean hasSchema(String tenantId);
    void createSchema(String tenantId);
    void dropSchema(String tenantId);
}
