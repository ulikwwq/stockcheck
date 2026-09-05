-- Phase 3: business (tenant) lifecycle management for SUPER_ADMIN.
--
-- status replaces "just exists" semantics: an INACTIVE business is the
-- soft-delete state (its users can no longer log in), matching the
-- project's soft-delete policy of never hard-deleting business data.
--
-- owner_user_id records which user is the business owner/administrator
-- so SUPER_ADMIN can look up and reset that owner's password directly,
-- without guessing which of a tenant's users holds the ADMINISTRATOR role.

ALTER TABLE tenants ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE tenants ADD CONSTRAINT chk_tenants_status
    CHECK (status IN ('ACTIVE', 'INACTIVE'));

ALTER TABLE tenants ADD COLUMN owner_user_id UUID;

ALTER TABLE tenants ADD CONSTRAINT fk_tenants_owner_user
    FOREIGN KEY (owner_user_id) REFERENCES users (id);

-- Backfill owner_user_id for any tenant that already has exactly one
-- ADMINISTRATOR (true for every tenant created by the existing
-- TenantService.createTenant flow).
UPDATE tenants t
SET owner_user_id = (
    SELECT ur.user_id
    FROM user_roles ur
    JOIN users u ON u.id = ur.user_id
    WHERE u.tenant_id = t.id AND ur.role = 'ADMINISTRATOR'
    LIMIT 1
)
WHERE t.owner_user_id IS NULL;
