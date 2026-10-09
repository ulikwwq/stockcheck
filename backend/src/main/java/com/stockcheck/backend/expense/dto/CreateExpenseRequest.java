package com.stockcheck.backend.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateExpenseRequest {

    @NotNull(message = "Укажите сумму расхода")
    @DecimalMin(value = "0.01", message = "Сумма расхода должна быть больше нуля")
    @Digits(integer = 10, fraction = 2, message = "Сумма должна содержать не более двух знаков после запятой")
    private BigDecimal amount;

    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String title;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    public CreateExpenseRequest() {
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
}