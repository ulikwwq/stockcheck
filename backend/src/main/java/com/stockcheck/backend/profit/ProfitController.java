package com.stockcheck.backend.profit;

import com.stockcheck.backend.profit.dto.DailyProfitResponse;
import com.stockcheck.backend.profit.dto.ProfitSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profit")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class ProfitController {

    private final ProfitService profitService;

    public ProfitController(ProfitService profitService) {
        this.profitService = profitService;
    }

    @GetMapping
    public ResponseEntity<ProfitSummaryResponse> getProfitSummary() {
        return ResponseEntity.ok(profitService.getProfitSummary());
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailyProfitResponse>> getDailyProfit() {
        return ResponseEntity.ok(profitService.getDailyProfit());
    }
}
