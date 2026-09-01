package com.stockcheck.backend.role;

/**
 * The fixed set of roles supported by the platform.
 *
 * <p>This is a closed, compile-time enumeration rather than a
 * user-manageable database table, since the set of roles is defined by the
 * StockCheck specification and is not expected to change at runtime. The
 * database still enforces the same closed set via a {@code CHECK}
 * constraint on {@code user_roles.role} (see {@code V2__tenant_user_shop.sql}),
 * so the enum and the schema cannot drift apart.
 *
 * <p>Full authorization behavior (what each role can and cannot do) is
 * implemented in a later Phase 2 step; this step only establishes the
 * domain/database model for role assignment.
 */
public enum RoleName {
    BUYER,
    SELLER,
    ADMINISTRATOR,
    MANAGER,
    COURIER,
    WAREHOUSE_OPERATOR,
    ACCOUNTANT,
    CONTENT_MANAGER,
    SUPER_ADMIN
}
