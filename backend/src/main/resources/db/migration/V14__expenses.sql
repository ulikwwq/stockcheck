CREATE TABLE expenses (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL,
    user_id     UUID,
    amount      NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    title       VARCHAR(255),
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_expenses_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),

    CONSTRAINT fk_expenses_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE INDEX idx_expenses_tenant_id
    ON expenses (tenant_id);

CREATE INDEX idx_expenses_created_at
    ON expenses (created_at);