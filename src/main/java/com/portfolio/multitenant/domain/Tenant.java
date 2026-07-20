package com.portfolio.multitenant.domain;

import java.time.Instant;
import java.util.Objects;

public class Tenant {

    public enum Status {
        ACTIVE, INACTIVE, PROVISIONING
    }

    private String id;
    private String name;
    private String schema;
    private Status status;
    private Instant createdAt;

    public Tenant() {}

    public Tenant(String id, String name, String schema, Status status, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.schema = schema;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tenant tenant = (Tenant) o;
        return Objects.equals(id, tenant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tenant{id='" + id + "', name='" + name + "', schema='" + schema + "', status=" + status + "}";
    }
}
