package com.stockcheck.backend.expense;

import com.stockcheck.backend.expense.dto.CreateExpenseRequest;
import com.stockcheck.backend.expense.dto.ExpenseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SELLER')")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses() {
        return ResponseEntity.ok(expenseService.getExpenses());
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        ExpenseResponse response = expenseService.createExpense(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}