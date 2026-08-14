package com.portfolio.multitenant.domain;

import java.util.List;

public interface TenantRecordRepository {
    TenantRecord insert(TenantRecord record);

    List<TenantRecord> findAllForCurrentTenant();
}
