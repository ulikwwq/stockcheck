package com.stockcheck.backend.sale.dto;

import com.stockcheck.backend.sale.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SaleResponse {

    private UUID id;
    private UUID shopId;
    private String shopName;
    private UUID sellerId;
    private String sellerName;
    private BigDecimal totalAmount;
    private List<SaleItemResponse> items;
    private LocalDateTime createdAt;

    public SaleResponse() {
    }

    public static SaleResponse fromEntity(Sale sale, boolean includeSensitiveInfo) {
        SaleResponse response = new SaleResponse();
        response.setId(sale.getId());
        response.setShopId(sale.getShop() != null ? sale.getShop().getId() : null);
        response.setShopName(sale.getShop() != null ? sale.getShop().getName() : null);
        response.setSellerId(sale.getSeller() != null ? sale.getSeller().getId() : null);
        if (sale.getSeller() != null) {
            response.setSellerName(sale.getSeller().getDisplayName());
        }
        response.setTotalAmount(sale.getTotalAmount());
        response.setCreatedAt(sale.getCreatedAt());

        if (sale.getItems() != null) {
            response.setItems(sale.getItems().stream()
                    .map(item -> SaleItemResponse.fromEntity(item, includeSensitiveInfo))
                    .toList());
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

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SaleItemResponse> getItems() {
        return items;
    }

    public void setItems(List<SaleItemResponse> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
