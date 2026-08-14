CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    schema_name VARCHAR(63) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    activated_at TIMESTAMPTZ,
    CONSTRAINT uq_tenants_name UNIQUE (name),
    CONSTRAINT uq_tenants_schema UNIQUE (schema_name),
    CONSTRAINT ck_tenants_name_nonblank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_tenants_schema_name CHECK (schema_name ~ '^tenant_[0-9a-f]{32}$'),
    CONSTRAINT ck_tenants_status CHECK (status IN ('PROVISIONING', 'ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_tenants_activation CHECK (
        (status = 'ACTIVE' AND activated_at IS NOT NULL)
        OR (status <> 'ACTIVE' AND activated_at IS NULL)
    )
);
