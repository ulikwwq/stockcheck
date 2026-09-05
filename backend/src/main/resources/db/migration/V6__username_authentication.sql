-- Phase 3: switch authentication identifier from phone number to username.
--
-- Username is unique GLOBALLY (not per-tenant): the login screen only asks
-- for username + password with no business selector, so two different
-- businesses cannot share a username the way they previously could share
-- a phone number.
--
-- first_name / last_name become optional: the business owner and sellers
-- may be created with only a username and password.

ALTER TABLE users ADD COLUMN username VARCHAR(50);

-- Backfill: the only pre-existing row is the seeded SUPER_ADMIN from V5.
UPDATE users SET username = 'superadmin' WHERE id = '00000000-0000-0000-0000-000000000002';

-- Safety net for any other pre-existing rows in an already-running
-- environment: derive a deterministic username from the row id so the
-- NOT NULL constraint below can never fail.
UPDATE users SET username = 'user_' || substr(replace(id::text, '-', ''), 1, 12)
WHERE username IS NULL;

ALTER TABLE users ALTER COLUMN username SET NOT NULL;

ALTER TABLE users ADD CONSTRAINT uq_users_username UNIQUE (username);

ALTER TABLE users ADD CONSTRAINT chk_users_username_format
    CHECK (username ~ '^[A-Za-z0-9_.]{3,50}$');

ALTER TABLE users DROP CONSTRAINT uq_users_tenant_phone;
ALTER TABLE users DROP COLUMN phone;

ALTER TABLE users ALTER COLUMN first_name DROP NOT NULL;
ALTER TABLE users ALTER COLUMN last_name DROP NOT NULL;
