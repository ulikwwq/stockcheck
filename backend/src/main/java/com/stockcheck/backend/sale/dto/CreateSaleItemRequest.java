package com.stockcheck.backend.sale.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateSaleItemRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Custom sale price cannot be negative")
    private BigDecimal customSalePrice;

    public CreateSaleItemRequest() {
    }

    public CreateSaleItemRequest(UUID productId, int quantity, BigDecimal customSalePrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.customSalePrice = customSalePrice;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getCustomSalePrice() {
        return customSalePrice;
    }

    public void setCustomSalePrice(BigDecimal customSalePrice) {
        this.customSalePrice = customSalePrice;
    }
}
