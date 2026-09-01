package com.stockcheck.backend.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangeUsernameRequest {

    @NotBlank(message = "Логин обязателен")
    @Pattern(regexp = "^[A-Za-z0-9_.]{3,50}$", message = "Логин может содержать только латинские буквы, цифры, '_' и '.'")
    private String newUsername;

    public ChangeUsernameRequest() {
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}
