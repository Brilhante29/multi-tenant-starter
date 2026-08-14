CREATE TABLE IF NOT EXISTS ${schema}.schema_history (
    version INTEGER PRIMARY KEY,
    description VARCHAR(120) NOT NULL,
    installed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ${schema}.tenant_records (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    payload VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_tenant_records_payload_nonblank CHECK (btrim(payload) <> ''),
    CONSTRAINT ck_tenant_records_owner CHECK (tenant_id = '${tenant_id}'::uuid)
);

CREATE INDEX IF NOT EXISTS ix_tenant_records_created_at
    ON ${schema}.tenant_records (created_at, id);

INSERT INTO ${schema}.schema_history (version, description)
VALUES (1, 'tenant records')
ON CONFLICT (version) DO NOTHING;
