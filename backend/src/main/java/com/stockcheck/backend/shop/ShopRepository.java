package com.stockcheck.backend.shop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {

    List<Shop> findByTenantId(UUID tenantId);
}
