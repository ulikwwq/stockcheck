package com.stockcheck.backend.analytics;

import com.stockcheck.backend.analytics.AnalyticsDtos.MonthPoint;
import com.stockcheck.backend.analytics.AnalyticsDtos.ProductHistory;
import com.stockcheck.backend.analytics.AnalyticsDtos.SummaryResponse;
import com.stockcheck.backend.analytics.AnalyticsDtos.TopProduct;
import com.stockcheck.backend.analytics.AnalyticsDtos.TopSort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public SummaryResponse summary(
            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate to,

            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate compareFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate compareTo
    ) {
        return service.summary(from, to, compareFrom, compareTo);
    }

    @GetMapping("/monthly")
    public List<MonthPoint> monthly(
            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate to
    ) {
        return service.monthly(from, to);
    }

    @GetMapping("/top-products")
    public List<TopProduct> top(
            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate to,

            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate compareFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate compareTo,

            @RequestParam(required = false)
            String query,

            @RequestParam(required = false)
            TopSort sort,

            @RequestParam(required = false)
            Integer limit
    ) {
        return service.topProducts(
                from,
                to,
                compareFrom,
                compareTo,
                query,
                sort,
                limit
        );
    }

    @GetMapping("/products/{id}/history")
    public ProductHistory history(
            @PathVariable UUID id,

            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate to
    ) {
        return service.productHistory(id, from, to);
    }
}