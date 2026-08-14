package com.portfolio.multitenant.domain;

import java.time.Instant;
import java.util.Objects;

public record TenantRecord(String id, String tenantId, String payload, Instant createdAt) {

    public TenantRecord {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(payload);
        Objects.requireNonNull(createdAt);
        if (payload.isBlank()) {
            throw new IllegalArgumentException("payload must not be blank");
        }
    }
}
