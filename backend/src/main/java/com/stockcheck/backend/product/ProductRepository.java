package com.stockcheck.backend.product;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByShopId(UUID shopId);

    @Query("SELECT p FROM Product p JOIN FETCH p.shop s WHERE s.tenant.id = :tenantId")
    List<Product> findByShopTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT p FROM Product p JOIN FETCH p.shop s WHERE p.id = :productId AND s.tenant.id = :tenantId")
    Optional<Product> findByIdAndShopTenantId(@Param("productId") UUID productId, @Param("tenantId") UUID tenantId);

    /**
     * Row-locking variant used when selling: holds a DB-level write lock on
     * the product row for the duration of the sale transaction so two
     * concurrent sales of the last unit cannot both read the same quantity
     * and both succeed.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p JOIN FETCH p.shop s WHERE p.id = :productId AND s.tenant.id = :tenantId")
    Optional<Product> findByIdAndShopTenantIdForUpdate(@Param("productId") UUID productId, @Param("tenantId") UUID tenantId);
}
