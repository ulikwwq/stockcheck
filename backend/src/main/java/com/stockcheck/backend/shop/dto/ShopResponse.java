package com.stockcheck.backend.shop.dto;

import com.stockcheck.backend.shop.Shop;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShopResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String address;
    private boolean active;
    private LocalDateTime createdAt;

    public ShopResponse() {
    }

    public ShopResponse(UUID id, UUID tenantId, String name, String address, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.address = address;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static ShopResponse fromEntity(Shop shop) {
        return new ShopResponse(
                shop.getId(),
                shop.getTenant() != null ? shop.getTenant().getId() : null,
                shop.getName(),
                shop.getAddress(),
                shop.isActive(),
                shop.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
