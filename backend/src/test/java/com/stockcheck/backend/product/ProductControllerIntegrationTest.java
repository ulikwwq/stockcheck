package com.stockcheck.backend.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockcheck.backend.common.GlobalExceptionHandler;
import com.stockcheck.backend.config.SecurityConfig;
import com.stockcheck.backend.product.dto.CreateProductRequest;
import com.stockcheck.backend.product.dto.ProductResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import({
        SecurityConfig.class,
        JwtProperties.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private com.stockcheck.backend.product.report.ProductReportService productReportService;

    private CreateProductRequest buildRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Leather Jacket");
        request.setSku("LJ-01");
        request.setDescription("Genuine leather");
        request.setPurchasePrice(new BigDecimal("10000.00"));
        request.setDefaultSalePrice(new BigDecimal("18000.00"));
        request.setQuantity(5);
        return request;
    }

    @Test
    @DisplayName("POST /api/v1/products allows ADMINISTRATOR to create product")
    void shouldAllowAdminToCreateProduct() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal adminPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "shop_admin", "Admin", "User", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(adminPrincipal);

        UUID productId = UUID.randomUUID();
        ProductResponse response = new ProductResponse();
        response.setId(productId);
        response.setName("Leather Jacket");
        response.setPurchasePrice(new BigDecimal("10000.00"));
        response.setDefaultSalePrice(new BigDecimal("18000.00"));
        response.setQuantity(5);

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Leather Jacket"))
                .andExpect(jsonPath("$.purchasePrice").value(10000.00));
    }

    @Test
    @DisplayName("POST /api/v1/products forbids SELLER from creating product")
    void shouldForbidSellerFromCreatingProduct() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal sellerPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "seller01", "Seller", "User", "", true, List.of("SELLER")
        );
        String token = jwtService.generateToken(sellerPrincipal);

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("POST /api/v1/products succeeds with only name and quantity (all other fields optional)")
    void shouldAllowCreatingProductWithMinimalFields() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal adminPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "shop_admin", "Admin", "User", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(adminPrincipal);

        UUID productId = UUID.randomUUID();
        ProductResponse response = new ProductResponse();
        response.setId(productId);
        response.setName("Кофе");
        response.setQuantity(20);

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Кофе");
        request.setQuantity(20);

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Кофе"));
    }

    @Test
    @DisplayName("POST /api/v1/products rejects blank product name")
    void shouldRejectBlankName() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal adminPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "shop_admin", "Admin", "User", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(adminPrincipal);

        CreateProductRequest request = new CreateProductRequest();
        request.setName("");
        request.setQuantity(1);

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products allows SELLER to view products with masked purchase price")
    void shouldAllowSellerToViewProducts() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal sellerPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "seller01", "Seller", "User", "", true, List.of("SELLER")
        );
        String token = jwtService.generateToken(sellerPrincipal);

        ProductResponse response = new ProductResponse();
        response.setId(UUID.randomUUID());
        response.setName("Leather Jacket");
        response.setPurchasePrice(null); // masked for seller
        response.setDefaultSalePrice(new BigDecimal("18000.00"));
        response.setQuantity(5);

        when(productService.getProducts(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Leather Jacket"))
                .andExpect(jsonPath("$[0].defaultSalePrice").value(18000.00))
                .andExpect(jsonPath("$[0].purchasePrice").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/products/report/pdf returns a PDF for ADMINISTRATOR")
    void shouldReturnPdfForAdministrator() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal adminPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "shop_admin", "Admin", "User", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(adminPrincipal);

        byte[] fakePdfBytes = "%PDF-1.4 fake".getBytes();
        when(productReportService.generateInventoryReportPdf()).thenReturn(fakePdfBytes);

        mockMvc.perform(get("/api/v1/products/report/pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("GET /api/v1/products/report/pdf returns a PDF for SELLER too")
    void shouldReturnPdfForSeller() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal sellerPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "seller01", "Seller", "User", "", true, List.of("SELLER")
        );
        String token = jwtService.generateToken(sellerPrincipal);

        byte[] fakePdfBytes = "%PDF-1.4 fake".getBytes();
        when(productReportService.generateInventoryReportPdf()).thenReturn(fakePdfBytes);

        mockMvc.perform(get("/api/v1/products/report/pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("GET /api/v1/products/report/pdf returns 401 Unauthorized when not authenticated")
    void shouldReturn401ForPdfWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/products/report/pdf"))
                .andExpect(status().isUnauthorized());
    }
}
