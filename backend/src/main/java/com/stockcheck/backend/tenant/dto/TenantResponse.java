package com.stockcheck.backend.tenant.dto;

import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class TenantResponse {

    private UUID id;
    private String name;
    private TenantStatus status;
    private UUID ownerUserId;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TenantResponse() {
    }

    public static TenantResponse fromEntity(Tenant tenant) {
        TenantResponse response = new TenantResponse();
        response.id = tenant.getId();
        response.name = tenant.getName();
        response.status = tenant.getStatus();
        response.createdAt = tenant.getCreatedAt();
        response.updatedAt = tenant.getUpdatedAt();
        if (tenant.getOwner() != null) {
            response.ownerUserId = tenant.getOwner().getId();
            response.ownerUsername = tenant.getOwner().getUsername();
        }
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(UUID ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
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
