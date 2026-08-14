package com.portfolio.multitenant.domain;

import java.time.Instant;
import java.util.Objects;

public final class Tenant {

    public enum Status {
        ACTIVE, INACTIVE, PROVISIONING
    }

    private final String id;
    private final String name;
    private final String schemaName;
    private final Status status;
    private final Instant createdAt;
    private final Instant activatedAt;

    public Tenant(
        String id,
        String name,
        String schemaName,
        Status status,
        Instant createdAt,
        Instant activatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.schemaName = Objects.requireNonNull(schemaName);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.activatedAt = activatedAt;
    }

    public Tenant(String id, String name, String schemaName, Status status, Instant createdAt) {
        this(id, name, schemaName, status, createdAt, status == Status.ACTIVE ? createdAt : null);
    }

    public Tenant activate(Instant at) {
        return new Tenant(id, name, schemaName, Status.ACTIVE, createdAt, at);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSchema() {
        return schemaName;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Tenant tenant && id.equals(tenant.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
