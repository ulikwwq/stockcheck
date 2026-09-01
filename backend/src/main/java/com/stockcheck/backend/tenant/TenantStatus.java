package com.stockcheck.backend.tenant;

/**
 * Lifecycle status of a business (tenant).
 *
 * <p>INACTIVE is the platform's soft-delete state for a business: the
 * business and its data are preserved, but its users can no longer log in.
 */
public enum TenantStatus {
    ACTIVE,
    INACTIVE,
    /** Terminal soft-delete state. Data is preserved; login is blocked. */
    DELETED
}
