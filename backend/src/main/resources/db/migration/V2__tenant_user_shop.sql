-- Phase 2, Part 1: Tenant / User / Shop domain and role assignment.
--
-- gen_random_uuid() is a built-in PostgreSQL function since PostgreSQL 13,
-- so no extension (e.g. pgcrypto) needs to be created first.

CREATE TABLE tenants (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL,
    phone         VARCHAR(32) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),

    -- The phone number is the login identifier and must be unique within
    -- a tenant, not globally: two different businesses may each have a
    -- user with the same phone number.
    CONSTRAINT uq_users_tenant_phone
        UNIQUE (tenant_id, phone)
);

CREATE INDEX idx_users_tenant_id ON users (tenant_id);

CREATE TABLE shops (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id  UUID NOT NULL,
    name       VARCHAR(255) NOT NULL,
    address    VARCHAR(500),
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shops_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id)
);

CREATE INDEX idx_shops_tenant_id ON shops (tenant_id);

-- Role assignment. The set of valid roles is a fixed, compile-time
-- enumeration (see com.stockcheck.backend.role.RoleName); the CHECK
-- constraint keeps the database in lockstep with that enum so the two
-- cannot silently drift apart. A user may be assigned more than one role.
CREATE TABLE user_roles (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL,
    role       VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),

    CONSTRAINT uq_user_roles_user_role
        UNIQUE (user_id, role),

    CONSTRAINT chk_user_roles_role
        CHECK (role IN (
            'BUYER',
            'SELLER',
            'ADMINISTRATOR',
            'MANAGER',
            'COURIER',
            'WAREHOUSE_OPERATOR',
            'ACCOUNTANT',
            'CONTENT_MANAGER',
            'SUPER_ADMIN'
        ))
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
