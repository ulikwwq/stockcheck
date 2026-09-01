package com.stockcheck.backend.profit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyProfitResponse {

    private LocalDate date;
    private BigDecimal revenue;
    private BigDecimal cost;
    private BigDecimal profit;
    /** True if at least one sold unit that day had no recorded purchase price. */
    private boolean profitPartiallyUnavailable;

    public DailyProfitResponse() {
    }

    public DailyProfitResponse(LocalDate date, BigDecimal revenue, BigDecimal cost, BigDecimal profit, boolean profitPartiallyUnavailable) {
        this.date = date;
        this.revenue = revenue;
        this.cost = cost;
        this.profit = profit;
        this.profitPartiallyUnavailable = profitPartiallyUnavailable;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public boolean isProfitPartiallyUnavailable() {
        return profitPartiallyUnavailable;
    }

    public void setProfitPartiallyUnavailable(boolean profitPartiallyUnavailable) {
        this.profitPartiallyUnavailable = profitPartiallyUnavailable;
    }
}
