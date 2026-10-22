package com.stockcheck.backend.tenant;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TenantHardDeleteRepository extends Repository<Tenant, UUID> {

    @Modifying
    @Query(value = """
            DELETE FROM audit_logs
            WHERE tenant_id = :tenantId
            """, nativeQuery = true)
    int deleteAuditLogs(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM expenses
            WHERE tenant_id = :tenantId
            """, nativeQuery = true)
    int deleteExpenses(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM stock_movements
            WHERE product_id IN (
                SELECT p.id
                FROM products p
                JOIN shops s ON s.id = p.shop_id
                WHERE s.tenant_id = :tenantId
            )
            """, nativeQuery = true)
    int deleteStockMovements(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM sale_items
            WHERE sale_id IN (
                SELECT sa.id
                FROM sales sa
                JOIN shops s ON s.id = sa.shop_id
                WHERE s.tenant_id = :tenantId
            )
            """, nativeQuery = true)
    int deleteSaleItems(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM sales
            WHERE shop_id IN (
                SELECT id FROM shops WHERE tenant_id = :tenantId
            )
            """, nativeQuery = true)
    int deleteSales(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM products
            WHERE shop_id IN (
                SELECT id FROM shops WHERE tenant_id = :tenantId
            )
            """, nativeQuery = true)
    int deleteProducts(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM categories
            WHERE tenant_id = :tenantId
            """, nativeQuery = true)
    int deleteCategories(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM user_roles
            WHERE user_id IN (
                SELECT id FROM users WHERE tenant_id = :tenantId
            )
            """, nativeQuery = true)
    int deleteUserRoles(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            UPDATE tenants
            SET owner_user_id = NULL
            WHERE id = :tenantId
            """, nativeQuery = true)
    int clearTenantOwner(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM users
            WHERE tenant_id = :tenantId
            """, nativeQuery = true)
    int deleteUsers(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM shops
            WHERE tenant_id = :tenantId
            """, nativeQuery = true)
    int deleteShops(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = """
            DELETE FROM tenants
            WHERE id = :tenantId
            """, nativeQuery = true)
    int deleteTenant(@Param("tenantId") UUID tenantId);
}