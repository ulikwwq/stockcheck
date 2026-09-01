package com.stockcheck.backend.profit;

import com.stockcheck.backend.common.GlobalExceptionHandler;
import com.stockcheck.backend.config.SecurityConfig;
import com.stockcheck.backend.profit.dto.ProfitSummaryResponse;
import com.stockcheck.backend.security.JwtAuthenticationFilter;
import com.stockcheck.backend.security.JwtProperties;
import com.stockcheck.backend.security.JwtService;
import com.stockcheck.backend.security.RestAccessDeniedHandler;
import com.stockcheck.backend.security.RestAuthenticationEntryPoint;
import com.stockcheck.backend.security.StockcheckPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfitController.class)
@Import({
        SecurityConfig.class,
        JwtProperties.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class ProfitControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ProfitService profitService;

    @Test
    @DisplayName("GET /api/v1/profit returns profit summary for ADMINISTRATOR")
    void shouldReturnProfitForAdmin() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal adminPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "+79991112233", "Admin", "User", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(adminPrincipal);

        ProfitSummaryResponse summary = new ProfitSummaryResponse(
                new BigDecimal("50000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("20000.00"),
                15
        );
        when(profitService.getProfitSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/profit")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(50000.00))
                .andExpect(jsonPath("$.totalCost").value(30000.00))
                .andExpect(jsonPath("$.totalProfit").value(20000.00))
                .andExpect(jsonPath("$.totalSalesCount").value(15));
    }

    @Test
    @DisplayName("GET /api/v1/profit returns 403 Forbidden for SELLER")
    void shouldForbidSellerFromViewingProfit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal sellerPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "+79998881122", "Seller", "User", "", true, List.of("SELLER")
        );
        String token = jwtService.generateToken(sellerPrincipal);

        mockMvc.perform(get("/api/v1/profit")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
