package com.stockcheck.backend.inventory;

import java.time.LocalDateTime;
import java.util.UUID;

public class StockMovementResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private StockMovementType type;
    private int quantityChange;
    private UUID userId;
    private String userName;
    private LocalDateTime createdAt;

    public StockMovementResponse() {
    }

    public static StockMovementResponse fromEntity(StockMovement movement) {
        StockMovementResponse response = new StockMovementResponse();
        response.setId(movement.getId());
        response.setProductId(movement.getProduct() != null ? movement.getProduct().getId() : null);
        response.setProductName(movement.getProduct() != null ? movement.getProduct().getName() : null);
        response.setType(movement.getType());
        response.setQuantityChange(movement.getQuantityChange());
        response.setUserId(movement.getUser() != null ? movement.getUser().getId() : null);
        if (movement.getUser() != null) {
            response.setUserName(movement.getUser().getDisplayName());
        }
        response.setCreatedAt(movement.getCreatedAt());
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

    public StockMovementType getType() {
        return type;
    }

    public void setType(StockMovementType type) {
        this.type = type;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
