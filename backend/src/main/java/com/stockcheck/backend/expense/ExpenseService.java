package com.stockcheck.backend.expense;

import com.stockcheck.backend.audit.AuditLog;
import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.expense.dto.CreateExpenseRequest;
import com.stockcheck.backend.expense.dto.ExpenseResponse;
import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses() {
        UUID tenantId = getCurrentTenantId();

        return expenseRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(ExpenseResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        UUID tenantId = getCurrentTenantId();

        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user context is required"
                ));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business not found"
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        String title = normalize(request.getTitle());
        String description = normalize(request.getDescription());

        Expense expense = new Expense(
                tenant,
                user,
                request.getAmount(),
                title,
                description
        );

        Expense savedExpense = expenseRepository.save(expense);

        StringBuilder details = new StringBuilder()
                .append("Сумма: ")
                .append(savedExpense.getAmount().toPlainString());

        if (title != null) {
            details.append("; Название: ").append(title);
        }

        if (description != null) {
            details.append("; Описание: ").append(description);
        }

        auditLogRepository.save(new AuditLog(
                tenant,
                user,
                "EXPENSE_CREATED",
                "EXPENSE",
                savedExpense.getId(),
                details.toString()
        ));

        return ExpenseResponse.fromEntity(savedExpense);
    }

    private UUID getCurrentTenantId() {
        return SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated tenant context is required"
                ));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
