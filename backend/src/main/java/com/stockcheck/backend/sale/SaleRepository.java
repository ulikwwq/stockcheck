package com.stockcheck.backend.sale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findByShopIdOrderByCreatedAtDesc(UUID shopId);

    @Query("SELECT s FROM Sale s JOIN FETCH s.shop sh WHERE sh.tenant.id = :tenantId ORDER BY s.createdAt DESC")
    List<Sale> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT s FROM Sale s JOIN FETCH s.shop sh WHERE s.id = :saleId AND sh.tenant.id = :tenantId")
    Optional<Sale> findByIdAndTenantId(@Param("saleId") UUID saleId, @Param("tenantId") UUID tenantId);

    /**
     * Bulk delete compiles to a single SQL DELETE, so the database's
     * ON DELETE CASCADE from sale_items to sales (see
     * V4__sales_inventory_audit_schema.sql) still removes the child rows
     * even though no entities are loaded into memory here.
     */
    @Modifying
    @Query("DELETE FROM Sale s WHERE s.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
