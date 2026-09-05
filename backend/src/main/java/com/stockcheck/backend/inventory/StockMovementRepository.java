package com.stockcheck.backend.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(UUID productId);

    @Query("SELECT sm FROM StockMovement sm JOIN FETCH sm.product p JOIN FETCH p.shop s WHERE s.tenant.id = :tenantId ORDER BY sm.createdAt DESC")
    List<StockMovement> findByTenantId(@Param("tenantId") UUID tenantId);

    @Modifying
    @Query("DELETE FROM StockMovement sm WHERE sm.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
