package com.stockcheck.backend.analytics;

import com.stockcheck.backend.analytics.AnalyticsDtos.*;
import com.stockcheck.backend.product.Product;
import com.stockcheck.backend.product.ProductRepository;
import com.stockcheck.backend.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final long MAX_RANGE_DAYS = 400;

    private final AnalyticsRepository repository;
    private final ProductRepository productRepository;

    public AnalyticsService(AnalyticsRepository repository, ProductRepository productRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
    }

    public SummaryResponse summary(LocalDate from, LocalDate to, LocalDate cmpFrom, LocalDate cmpTo) {
        UUID t = tenant();
        validate(from, to);
        LocalDate[] prev = previous(from, to, cmpFrom, cmpTo);
        return new SummaryResponse(from, to, prev[0], prev[1],
                repository.totals(t, start(from), end(to)), repository.totals(t, start(prev[0]), end(prev[1])));
    }

    public List<MonthPoint> monthly(LocalDate from, LocalDate to) {
        validate(from, to);
        return repository.monthly(tenant(), start(from), end(to));
    }

    public List<TopProduct> topProducts(LocalDate from, LocalDate to, LocalDate cmpFrom, LocalDate cmpTo,
                                        String query, TopSort sort, Integer limit) {
        validate(from, to);
        LocalDate[] prev = previous(from, to, cmpFrom, cmpTo);
        int lim = limit == null ? 50 : Math.min(Math.max(limit, 1), 200);
        return repository.topProducts(tenant(), start(from), end(to), start(prev[0]), end(prev[1]), query,
                sort == null ? TopSort.UNITS : sort, lim);
    }

    public ProductHistory productHistory(UUID productId, LocalDate from, LocalDate to) {
        UUID t = tenant();
        validate(from, to);
        Product product = productRepository.findByIdAndShopTenantId(productId, t)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден"));
        Map<LocalDate, ProductMonth> data = repository.productMonthly(t, productId, start(from), end(to));

        List<ProductMonth> months = new ArrayList<>();
        long units = 0; BigDecimal rev = BigDecimal.ZERO, cost = BigDecimal.ZERO, profit = BigDecimal.ZERO;
        for (LocalDate m = from.withDayOfMonth(1); !m.isAfter(to); m = m.plusMonths(1)) {
            ProductMonth pm = data.getOrDefault(m, new ProductMonth(m, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            months.add(pm);
            units += pm.unitsSold(); rev = rev.add(pm.revenue()); cost = cost.add(pm.cost()); profit = profit.add(pm.grossProfit());
        }
        return new ProductHistory(productId, product.getName(), months, new ProductMonth(null, units, rev, cost, profit));
    }

    private static void validate(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите даты начала и окончания");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата начала не может быть позже даты окончания");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Период не может превышать 12 месяцев");
        }
    }

    /** Если клиент не передал период сравнения — берём предыдущий интервал той же длины. */
    private static LocalDate[] previous(LocalDate from, LocalDate to, LocalDate cf, LocalDate ct) {
        if (cf != null && ct != null) {
            validate(cf, ct);
            return new LocalDate[]{cf, ct};
        }
        long len = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate pt = from.minusDays(1);
        return new LocalDate[]{pt.minusDays(len - 1), pt};
    }

    private static LocalDateTime start(LocalDate d) { return d.atStartOfDay(); }
    private static LocalDateTime end(LocalDate d) { return d.plusDays(1).atStartOfDay(); }

    private UUID tenant() {
        return SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));
    }
}