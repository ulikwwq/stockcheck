package com.stockcheck.backend.sale.dto;

import com.stockcheck.backend.sale.SaleItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class SaleItemResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal profit;
    private LocalDateTime createdAt;

    public SaleItemResponse() {
    }

    public static SaleItemResponse fromEntity(SaleItem item, boolean includeSensitiveInfo) {
        SaleItemResponse response = new SaleItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        response.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
        response.setQuantity(item.getQuantity());
        response.setSalePrice(item.getSalePrice());
        response.setCreatedAt(item.getCreatedAt());

        if (includeSensitiveInfo) {
            response.setPurchasePrice(item.getPurchasePrice());
            response.setProfit(item.getProfit());
        } else {
            response.setPurchasePrice(null);
            response.setProfit(null);
        }

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
