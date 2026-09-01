package com.stockcheck.backend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Only {@code name} and {@code quantity} are required per spec; purchase
 * price, sale price, category and photo are all optional. {@code shopId}
 * is intentionally absent — the administrator never picks a shop in the
 * simplified mobile UI, so the service resolves the tenant's shop itself.
 */
public class CreateProductRequest {

    private UUID categoryId;

    @NotBlank(message = "Введите название товара")
    @Size(max = 255, message = "Название товара не должно превышать 255 символов")
    private String name;

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    private String description;

    @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
    private String imageUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Цена закупки не может быть отрицательной")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Цена продажи не может быть отрицательной")
    private BigDecimal defaultSalePrice;

    @Min(value = 0, message = "Количество должно быть больше или равно нулю")
    private int quantity = 0;

    public CreateProductRequest() {
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
