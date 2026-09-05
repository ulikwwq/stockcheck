package com.stockcheck.backend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateProductRequest {

    private UUID categoryId;

    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    private String description;

    @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
    private String imageUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Default sale price cannot be negative")
    private BigDecimal defaultSalePrice;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private Boolean active;

    public UpdateProductRequest() {
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
