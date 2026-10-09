package com.stockcheck.backend.expense.dto;

import com.stockcheck.backend.expense.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExpenseResponse {

    private UUID id;
    private BigDecimal amount;
    private String title;
    private String description;
    private String userName;
    private LocalDateTime createdAt;

    public ExpenseResponse() {
    }

    public static ExpenseResponse fromEntity(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();

        response.setId(expense.getId());
        response.setAmount(expense.getAmount());
        response.setTitle(expense.getTitle());
        response.setDescription(expense.getDescription());
        response.setUserName(
                expense.getUser() != null
                        ? expense.getUser().getDisplayName()
                        : null
        );
        response.setCreatedAt(expense.getCreatedAt());

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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