package com.stockcheck.backend.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class AnalyticsDtos {
    private AnalyticsDtos() {}

    public record Totals(BigDecimal revenue, BigDecimal cost, BigDecimal grossProfit, BigDecimal expenses,
                         BigDecimal operatingResult, long salesCount, long unitsSold, long unknownCostUnits) {}

    public record SummaryResponse(LocalDate from, LocalDate to, LocalDate previousFrom, LocalDate previousTo,
                                  Totals current, Totals previous) {}

    public record MonthPoint(LocalDate month, BigDecimal revenue, BigDecimal cost, BigDecimal grossProfit,
                             BigDecimal expenses, BigDecimal operatingResult, long salesCount, long unitsSold) {}

    public record TopProduct(UUID productId, String name, String sku, long unitsSold, BigDecimal revenue,
                             BigDecimal cost, BigDecimal grossProfit, long salesCount, LocalDateTime lastSaleAt,
                             long previousUnitsSold, BigDecimal changePercent) {}

    public record ProductMonth(LocalDate month, long unitsSold, BigDecimal revenue, BigDecimal cost, BigDecimal grossProfit) {}

    public record ProductHistory(UUID productId, String name, List<ProductMonth> months, ProductMonth total) {}

    public enum TopSort { UNITS, REVENUE, PROFIT, SALES, TREND }
}