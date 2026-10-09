package com.stockcheck.backend.profit;

import com.stockcheck.backend.expense.Expense;
import com.stockcheck.backend.expense.ExpenseRepository;
import com.stockcheck.backend.profit.dto.DailyProfitResponse;
import com.stockcheck.backend.profit.dto.ProfitSummaryResponse;
import com.stockcheck.backend.sale.SaleItemRepository;
import com.stockcheck.backend.sale.SaleRepository;
import com.stockcheck.backend.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class ProfitService {

    /** Matches the platform's 30-day operational data retention window. */
    private static final int RETENTION_DAYS = 30;

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ExpenseRepository expenseRepository;

    public ProfitService(
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            ExpenseRepository expenseRepository
    ) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public ProfitSummaryResponse getProfitSummary() {
        UUID tenantId = currentTenantId();

        BigDecimal totalRevenue = saleItemRepository.calculateTotalRevenueByTenantId(tenantId);
        BigDecimal totalCost = saleItemRepository.calculateTotalCostByTenantId(tenantId);
        BigDecimal totalProfit = saleItemRepository.calculateTotalProfitByTenantId(tenantId);

        long totalSalesCount = saleRepository.findByTenantId(tenantId).stream()
                .filter(sale -> !sale.isReturned())
                .count();

        return new ProfitSummaryResponse(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalCost != null ? totalCost : BigDecimal.ZERO,
                totalProfit != null ? totalProfit : BigDecimal.ZERO,
                totalSalesCount
        );
    }

    /** Daily profit after expenses, with the most recent day first. */
    @Transactional(readOnly = true)
    public List<DailyProfitResponse> getDailyProfit() {
        UUID tenantId = currentTenantId();
        LocalDateTime since = LocalDateTime.now().minusDays(RETENTION_DAYS);

        Map<LocalDate, DailyProfitResponse> dailyResults =
                new TreeMap<>((first, second) -> second.compareTo(first));

        List<Object[]> rows = saleItemRepository.calculateDailyBreakdown(tenantId, since);

        for (Object[] row : rows) {
            LocalDate date = (LocalDate) row[0];

            BigDecimal revenue = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            BigDecimal cost = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            BigDecimal profit = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            boolean partiallyUnavailable = ((Number) row[4]).longValue() > 0;

            dailyResults.put(date, new DailyProfitResponse(
                    date,
                    revenue,
                    cost,
                    BigDecimal.ZERO,
                    profit,
                    partiallyUnavailable
            ));
        }

        List<Expense> expenses = expenseRepository
                .findByTenantIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                        tenantId,
                        since
                );

        for (Expense expense : expenses) {
            LocalDate date = expense.getCreatedAt().toLocalDate();

            DailyProfitResponse daily = dailyResults.computeIfAbsent(
                    date,
                    day -> new DailyProfitResponse(
                            day,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            false
                    )
            );

            BigDecimal amount = expense.getAmount();

            daily.setExpenses(daily.getExpenses().add(amount));
            daily.setProfit(daily.getProfit().subtract(amount));
        }

        return dailyResults.values().stream().toList();
    }

    private UUID currentTenantId() {
        return SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated tenant context is required"
                ));
    }

}