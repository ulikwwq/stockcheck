package com.stockcheck.backend.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogResponse {

    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String userName;
    private String action;
    private String entityType;
    private UUID entityId;
    private String details;
    private LocalDateTime createdAt;

    public AuditLogResponse() {
    }

    public static AuditLogResponse fromEntity(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setTenantId(log.getTenant() != null ? log.getTenant().getId() : null);
        response.setUserId(log.getUser() != null ? log.getUser().getId() : null);
        if (log.getUser() != null) {
            response.setUserName(log.getUser().getDisplayName());
        }
        response.setAction(log.getAction());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setDetails(log.getDetails());
        response.setCreatedAt(log.getCreatedAt());
        return response;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
