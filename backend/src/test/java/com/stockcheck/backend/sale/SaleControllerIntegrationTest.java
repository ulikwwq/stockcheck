package com.stockcheck.backend.sale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockcheck.backend.common.GlobalExceptionHandler;
import com.stockcheck.backend.config.SecurityConfig;
import com.stockcheck.backend.sale.dto.CreateSaleItemRequest;
import com.stockcheck.backend.sale.dto.CreateSaleRequest;
import com.stockcheck.backend.sale.dto.SaleItemResponse;
import com.stockcheck.backend.sale.dto.SaleResponse;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SaleController.class)
@Import({
        SecurityConfig.class,
        JwtProperties.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class SaleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private SaleService saleService;

    @Test
    @DisplayName("POST /api/v1/sales registers sale and returns 201 Created")
    void shouldRegisterSale() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal sellerPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "seller01", "Seller", "User", "", true, List.of("SELLER")
        );
        String token = jwtService.generateToken(sellerPrincipal);

        UUID saleId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        SaleItemResponse item = new SaleItemResponse();
        item.setId(UUID.randomUUID());
        item.setProductId(productId);
        item.setQuantity(2);
        item.setSalePrice(new BigDecimal("1500.00"));

        SaleResponse response = new SaleResponse();
        response.setId(saleId);
        response.setShopId(shopId);
        response.setTotalAmount(new BigDecimal("3000.00"));
        response.setItems(List.of(item));

        when(saleService.createSale(any(CreateSaleRequest.class))).thenReturn(response);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(new CreateSaleItemRequest(productId, 2, null)));

        mockMvc.perform(post("/api/v1/sales")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saleId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(3000.00));
    }
}
