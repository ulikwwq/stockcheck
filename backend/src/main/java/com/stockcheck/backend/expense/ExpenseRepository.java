package com.stockcheck.backend.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<Expense> findByTenantIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            UUID tenantId,
            LocalDateTime since
    );

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.tenant.id = :tenantId
            """)
    BigDecimal calculateTotalExpensesByTenantId(@Param("tenantId") UUID tenantId);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.tenant.id = :tenantId
              AND e.createdAt >= :start
              AND e.createdAt < :end
            """)
    BigDecimal calculateExpensesForPeriod(
            @Param("tenantId") UUID tenantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}