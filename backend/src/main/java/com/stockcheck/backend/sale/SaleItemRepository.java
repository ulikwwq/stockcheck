package com.stockcheck.backend.sale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    List<SaleItem> findBySaleId(UUID saleId);

    @Query("SELECT COALESCE(SUM(si.profit), 0) FROM SaleItem si JOIN si.sale s JOIN s.shop sh WHERE sh.tenant.id = :tenantId")
    BigDecimal calculateTotalProfitByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(si.salePrice * si.quantity), 0) FROM SaleItem si JOIN si.sale s JOIN s.shop sh WHERE sh.tenant.id = :tenantId")
    BigDecimal calculateTotalRevenueByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(si.purchasePrice * si.quantity), 0) FROM SaleItem si JOIN si.sale s JOIN s.shop sh WHERE sh.tenant.id = :tenantId")
    BigDecimal calculateTotalCostByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Per-day rollup used by the "Прибыль" screen. Each row is:
     * [0] day (java.sql.Date), [1] revenue, [2] cost (only known-cost items),
     * [3] profit (only known-cost items), [4] count of items with unknown
     * purchase price (profit not calculable for those units).
     */
    @Query(value = "SELECT CAST(s.created_at AS date) AS day, " +
            "COALESCE(SUM(si.sale_price * si.quantity), 0) AS revenue, " +
            "COALESCE(SUM(CASE WHEN si.purchase_price IS NOT NULL THEN si.purchase_price * si.quantity ELSE 0 END), 0) AS cost, " +
            "COALESCE(SUM(si.profit), 0) AS profit, " +
            "SUM(CASE WHEN si.purchase_price IS NULL THEN si.quantity ELSE 0 END) AS unknown_cost_units " +
            "FROM sale_items si " +
            "JOIN sales s ON s.id = si.sale_id " +
            "JOIN shops sh ON sh.id = s.shop_id " +
            "WHERE sh.tenant_id = :tenantId AND s.created_at >= :since " +
            "GROUP BY CAST(s.created_at AS date) " +
            "ORDER BY day DESC", nativeQuery = true)
    List<Object[]> calculateDailyBreakdown(@Param("tenantId") UUID tenantId, @Param("since") LocalDateTime since);
}
