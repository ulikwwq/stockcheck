package com.stockcheck.backend.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * shopId is intentionally absent: the seller/administrator never picks a
 * shop in the simplified mobile sale flow, so the service resolves the
 * tenant's shop itself.
 */
public class CreateSaleRequest {

    @NotEmpty(message = "Продажа должна содержать хотя бы один товар")
    @Valid
    private List<CreateSaleItemRequest> items;

    public CreateSaleRequest() {
    }

    public List<CreateSaleItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CreateSaleItemRequest> items) {
        this.items = items;
    }
}
