package com.stockcheck.backend.analytics;

import com.stockcheck.backend.analytics.AnalyticsDtos.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class AnalyticsRepository {

    /** Выручка/себестоимость — только по позициям с известной ценой; прибыль — только по полностью известным (поле profit). */
    private static final String AGG = """
            COALESCE(SUM(CASE WHEN si.sale_price IS NOT NULL THEN si.sale_price * si.quantity END), 0) AS revenue,
            COALESCE(SUM(CASE WHEN si.purchase_price IS NOT NULL THEN si.purchase_price * si.quantity END), 0) AS cost,
            COALESCE(SUM(si.profit), 0) AS profit,
            COUNT(DISTINCT s.id) AS sales_count,
            COALESCE(SUM(si.quantity), 0) AS units,
            COALESCE(SUM(CASE WHEN si.purchase_price IS NULL OR si.sale_price IS NULL THEN si.quantity ELSE 0 END), 0) AS unknown_units
            """;

    private static final String FROM_SALES = """
            FROM sale_items si
            JOIN sales s ON s.id = si.sale_id
            JOIN shops sh ON sh.id = s.shop_id
            """;

    private static final String SALES_WHERE =
            " WHERE sh.tenant_id = :t AND s.returned = false AND s.created_at >= :f AND s.created_at < :e ";

    private final NamedParameterJdbcTemplate jdbc;

    public AnalyticsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static MapSqlParameterSource range(UUID t, LocalDateTime f, LocalDateTime e) {
        return new MapSqlParameterSource("t", t).addValue("f", f).addValue("e", e);
    }

    public Totals totals(UUID tenant, LocalDateTime from, LocalDateTime toExcl) {
        MapSqlParameterSource p = range(tenant, from, toExcl);
        BigDecimal expenses = expensesTotal(p);
        return jdbc.queryForObject("SELECT " + AGG + FROM_SALES + SALES_WHERE, p, (rs, i) -> {
            BigDecimal profit = rs.getBigDecimal("profit");
            return new Totals(rs.getBigDecimal("revenue"), rs.getBigDecimal("cost"), profit, expenses,
                    profit.subtract(expenses), rs.getLong("sales_count"), rs.getLong("units"), rs.getLong("unknown_units"));
        });
    }

    private BigDecimal expensesTotal(MapSqlParameterSource p) {
        return jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE tenant_id = :t AND created_at >= :f AND created_at < :e",
                p, BigDecimal.class);
    }

    public List<MonthPoint> monthly(UUID tenant, LocalDateTime from, LocalDateTime toExcl) {
        MapSqlParameterSource p = range(tenant, from, toExcl);
        Map<LocalDate, MonthPoint> sales = new HashMap<>();
        jdbc.query("SELECT date_trunc('month', s.created_at)::date AS m, " + AGG + FROM_SALES + SALES_WHERE + " GROUP BY m", p, rs -> {
            LocalDate m = rs.getDate("m").toLocalDate();
            BigDecimal profit = rs.getBigDecimal("profit");
            sales.put(m, new MonthPoint(m, rs.getBigDecimal("revenue"), rs.getBigDecimal("cost"), profit,
                    BigDecimal.ZERO, profit, rs.getLong("sales_count"), rs.getLong("units")));
        });
        Map<LocalDate, BigDecimal> expenses = new HashMap<>();
        jdbc.query("SELECT date_trunc('month', created_at)::date AS m, SUM(amount) AS a FROM expenses " +
                        "WHERE tenant_id = :t AND created_at >= :f AND created_at < :e GROUP BY m", p,
                rs -> { expenses.put(rs.getDate("m").toLocalDate(), rs.getBigDecimal("a")); });

        List<MonthPoint> result = new ArrayList<>();
        LocalDate cursor = from.toLocalDate().withDayOfMonth(1);
        LocalDate last = toExcl.minusDays(1).toLocalDate().withDayOfMonth(1);
        while (!cursor.isAfter(last)) {
            MonthPoint s = sales.get(cursor);
            BigDecimal exp = expenses.getOrDefault(cursor, BigDecimal.ZERO);
            BigDecimal profit = s != null ? s.grossProfit() : BigDecimal.ZERO;
            result.add(new MonthPoint(cursor,
                    s != null ? s.revenue() : BigDecimal.ZERO, s != null ? s.cost() : BigDecimal.ZERO, profit, exp,
                    profit.subtract(exp), s != null ? s.salesCount() : 0, s != null ? s.unitsSold() : 0));
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    public List<TopProduct> topProducts(UUID tenant, LocalDateTime f, LocalDateTime e,
                                        LocalDateTime pf, LocalDateTime pe, String query, TopSort sort, int limit) {
        MapSqlParameterSource p = range(tenant, f, e).addValue("pf", pf).addValue("pe", pe).addValue("limit", limit);
        String filter = "";
        if (query != null && !query.isBlank()) {
            String escaped = query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            p.addValue("q", "%" + escaped + "%");
            filter = " AND (p.name ILIKE :q OR p.sku ILIKE :q) ";
        }
        String order = switch (sort) {
            case UNITS -> "cur.units DESC";
            case REVENUE -> "cur.revenue DESC";
            case PROFIT -> "cur.profit DESC";
            case SALES -> "cur.sales_count DESC";
            case TREND -> "CASE WHEN COALESCE(prev.units,0) > 0 THEN (cur.units - prev.units)::numeric / prev.units END DESC NULLS LAST";
        };
        String sql = """
                WITH cur AS (
                  SELECT p.id, p.name, p.sku, MAX(s.created_at) AS last_sale, %s
                  %s JOIN products p ON p.id = si.product_id
                  %s %s
                  GROUP BY p.id, p.name, p.sku),
                prev AS (
                  SELECT si.product_id, SUM(si.quantity) AS units
                  %s
                  WHERE sh.tenant_id = :t AND s.returned = false AND s.created_at >= :pf AND s.created_at < :pe
                  GROUP BY si.product_id)
                SELECT cur.*, COALESCE(prev.units, 0) AS prev_units
                FROM cur LEFT JOIN prev ON prev.product_id = cur.id
                ORDER BY %s, cur.name
                LIMIT :limit
                """.formatted(AGG, FROM_SALES, SALES_WHERE, filter, FROM_SALES, order);

        return jdbc.query(sql, p, (rs, i) -> {
            long units = rs.getLong("units");
            long prevUnits = rs.getLong("prev_units");
            BigDecimal change = prevUnits > 0
                    ? BigDecimal.valueOf(units - prevUnits).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(prevUnits), 1, RoundingMode.HALF_UP)
                    : null; // нет базы для сравнения — не выдумываем
            return new TopProduct((UUID) rs.getObject("id"), rs.getString("name"), rs.getString("sku"), units,
                    rs.getBigDecimal("revenue"), rs.getBigDecimal("cost"), rs.getBigDecimal("profit"),
                    rs.getLong("sales_count"), rs.getTimestamp("last_sale").toLocalDateTime(), prevUnits, change);
        });
    }

    public Map<LocalDate, ProductMonth> productMonthly(UUID tenant, UUID productId, LocalDateTime f, LocalDateTime e) {
        MapSqlParameterSource p = range(tenant, f, e).addValue("pid", productId);
        Map<LocalDate, ProductMonth> map = new HashMap<>();
        jdbc.query("SELECT date_trunc('month', s.created_at)::date AS m, " + AGG + FROM_SALES + SALES_WHERE +
                " AND si.product_id = :pid GROUP BY m", p, rs -> {
            LocalDate m = rs.getDate("m").toLocalDate();
            map.put(m, new ProductMonth(m, rs.getLong("units"), rs.getBigDecimal("revenue"),
                    rs.getBigDecimal("cost"), rs.getBigDecimal("profit")));
        });
        return map;
    }
}