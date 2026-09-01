package com.stockcheck.backend.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
