package com.stockcheck.backend.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateTenantRequest {

    @NotBlank(message = "Название бизнеса обязательно")
    @Size(max = 255, message = "Название бизнеса не должно превышать 255 символов")
    private String tenantName;

    private String shopName;

    @NotBlank(message = "Логин владельца обязателен")
    @Pattern(regexp = "^[A-Za-z0-9_.]{3,50}$", message = "Логин может содержать только латинские буквы, цифры, '_' и '.'")
    private String ownerUsername;

    @NotBlank(message = "Пароль владельца обязателен")
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String ownerPassword;

    /** Optional per spec: business owner first name is not mandatory. */
    private String ownerFirstName;

    /** Optional per spec: business owner last name is not mandatory. */
    private String ownerLastName;

    public CreateTenantRequest() {
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public void setOwnerFirstName(String ownerFirstName) {
        this.ownerFirstName = ownerFirstName;
    }

    public String getOwnerLastName() {
        return ownerLastName;
    }

    public void setOwnerLastName(String ownerLastName) {
        this.ownerLastName = ownerLastName;
    }
}
