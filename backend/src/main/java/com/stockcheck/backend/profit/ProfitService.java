package com.stockcheck.backend.profit;

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
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProfitService {

    /** Matches the platform's 30-day operational data retention window. */
    private static final int RETENTION_DAYS = 30;

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;

    public ProfitService(SaleRepository saleRepository, SaleItemRepository saleItemRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
    }

    @Transactional(readOnly = true)
    public ProfitSummaryResponse getProfitSummary() {
        UUID tenantId = currentTenantId();

        BigDecimal totalRevenue = saleItemRepository.calculateTotalRevenueByTenantId(tenantId);
        BigDecimal totalCost = saleItemRepository.calculateTotalCostByTenantId(tenantId);
        BigDecimal totalProfit = saleItemRepository.calculateTotalProfitByTenantId(tenantId);
        long totalSalesCount = saleRepository.findByTenantId(tenantId).size();

        return new ProfitSummaryResponse(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalCost != null ? totalCost : BigDecimal.ZERO,
                totalProfit != null ? totalProfit : BigDecimal.ZERO,
                totalSalesCount
        );
    }

    /** Per-day revenue/cost/profit for the "Прибыль" screen, most recent day first. */
    @Transactional(readOnly = true)
    public List<DailyProfitResponse> getDailyProfit() {
        UUID tenantId = currentTenantId();
        LocalDateTime since = LocalDateTime.now().minusDays(RETENTION_DAYS);

        List<Object[]> rows = saleItemRepository.calculateDailyBreakdown(tenantId, since);
        return rows.stream()
                .map(row -> new DailyProfitResponse(
                        ((Date) row[0]).toLocalDate(),
                        (BigDecimal) row[1],
                        (BigDecimal) row[2],
                        (BigDecimal) row[3],
                        ((Number) row[4]).longValue() > 0
                ))
                .toList();
    }

    private UUID currentTenantId() {
        return SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));
    }
}
