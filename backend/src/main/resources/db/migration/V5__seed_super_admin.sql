-- Bootstraps the platform with one SUPER_ADMIN account.
--
-- Every row in `users` requires a tenant_id (see V2), so even the
-- platform-level SUPER_ADMIN needs a tenant to belong to. Without this
-- seed, nobody could ever authenticate to call
-- POST /api/v1/admin/tenants, and the platform would be permanently
-- unusable out of the box.
--
-- These are local/development bootstrap credentials only. Rotate the
-- password (or delete this account and create a fresh one) before any
-- shared or production deployment.
--
-- pgcrypto's crypt()/gen_salt('bf', 10) produces a standard bcrypt hash
-- ($2a$ prefixed) fully compatible with Spring Security's
-- BCryptPasswordEncoder, so the seeded password can be verified by the
-- application exactly like any password set through the normal API.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO tenants (id, name)
VALUES ('00000000-0000-0000-0000-000000000001', 'StockCheck Platform')
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, tenant_id, phone, password_hash, first_name, last_name, active)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    '+10000000000',
    crypt('SuperAdmin123!', gen_salt('bf', 10)),
    'Platform',
    'Admin',
    TRUE
)
ON CONFLICT (tenant_id, phone) DO NOTHING;

INSERT INTO user_roles (user_id, role)
VALUES ('00000000-0000-0000-0000-000000000002', 'SUPER_ADMIN')
ON CONFLICT (user_id, role) DO NOTHING;
