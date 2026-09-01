-- Bug fix: SUPER_ADMIN needs a real "delete business" operation, distinct
-- from activate/deactivate. DELETED is a soft-delete terminal state: the
-- tenant row and all its historical data are preserved, but its users can
-- no longer log in (same effect as INACTIVE for login purposes).

ALTER TABLE tenants DROP CONSTRAINT chk_tenants_status;

ALTER TABLE tenants ADD CONSTRAINT chk_tenants_status
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'));
