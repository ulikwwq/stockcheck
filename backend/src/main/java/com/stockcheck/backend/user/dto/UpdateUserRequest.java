package com.stockcheck.backend.user.dto;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    private String firstName;
    private String lastName;

    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String newPassword;

    public UpdateUserRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
