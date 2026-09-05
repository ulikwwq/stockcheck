package com.stockcheck.backend.product.dto;

import com.stockcheck.backend.product.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductResponse {

    private UUID id;
    private UUID shopId;
    private String shopName;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String sku;
    private String description;
    private String imageUrl;
    private BigDecimal purchasePrice;
    private BigDecimal defaultSalePrice;
    private int quantity;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse() {
    }

    public static ProductResponse fromEntity(Product product, boolean includeSensitiveInfo) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setShopId(product.getShop() != null ? product.getShop().getId() : null);
        response.setShopName(product.getShop() != null ? product.getShop().getName() : null);
        response.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        response.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setDescription(product.getDescription());
        response.setImageUrl(product.getImageUrl());
        response.setDefaultSalePrice(product.getDefaultSalePrice());
        response.setQuantity(product.getQuantity());
        response.setActive(product.isActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Sensitive business data: purchase price is only returned to administrators/owners
        if (includeSensitiveInfo) {
            response.setPurchasePrice(product.getPurchasePrice());
        } else {
            response.setPurchasePrice(null);
        }

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getDefaultSalePrice() {
        return defaultSalePrice;
    }

    public void setDefaultSalePrice(BigDecimal defaultSalePrice) {
        this.defaultSalePrice = defaultSalePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
