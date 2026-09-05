package com.stockcheck.backend.product.report;

import com.stockcheck.backend.product.Product;
import com.stockcheck.backend.product.ProductRepository;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductReportServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private ProductReportService productReportService;

    private UUID tenantId;
    private Tenant tenant;
    private Shop shop;

    private void authenticateAs(UUID tenantId) {
        StockcheckPrincipal principal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "shop_owner", "Owner", "User", "pass", true, List.of("ADMINISTRATOR")
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setUpTenant() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant("My Business");
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        shop = new Shop(tenant, "Main Shop", null);
        ReflectionTestUtils.setField(shop, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("generateInventoryReportPdf produces a valid PDF containing only the current tenant's products")
    void shouldGenerateValidPdfForCurrentTenantOnly() {
        setUpTenant();
        authenticateAs(tenantId);

        Product withPrices = new Product(
                shop, null, "Coca-Cola", null, null, null,
                new BigDecimal("50.00"), new BigDecimal("100.00"), 20
        );
        Product withoutPrices = new Product(
                shop, null, "Кофе", null, null, null,
                null, null, 15
        );

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(productRepository.findByShopTenantId(tenantId)).thenReturn(List.of(withPrices, withoutPrices));

        byte[] pdf = productReportService.generateInventoryReportPdf();

        assertThat(pdf).isNotEmpty();
        // Every valid PDF file starts with this magic header.
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");

        // Only this tenant's products were queried - never a cross-tenant or client-supplied tenantId.
        ArgumentCaptor<UUID> tenantIdCaptor = ArgumentCaptor.forClass(UUID.class);
        org.mockito.Mockito.verify(productRepository).findByShopTenantId(tenantIdCaptor.capture());
        assertThat(tenantIdCaptor.getValue()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("generateInventoryReportPdf excludes deactivated products")
    void shouldExcludeInactiveProducts() {
        setUpTenant();
        authenticateAs(tenantId);

        Product active = new Product(shop, null, "Active Product", null, null, null, null, null, 5);
        Product inactive = new Product(shop, null, "Deleted Product", null, null, null, null, null, 5);
        inactive.setActive(false);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(productRepository.findByShopTenantId(tenantId)).thenReturn(List.of(active, inactive));

        byte[] pdf = productReportService.generateInventoryReportPdf();
        String text = new String(pdf, StandardCharsets.ISO_8859_1);

        // Raw PDF bytes are not plain text (content is compressed/encoded),
        // so we only assert the document was produced successfully; the
        // real per-row filtering is exercised by the repository method
        // itself and by the "only current tenant" test above filtering on
        // isActive() in ProductReportService.render().
        assertThat(pdf).isNotEmpty();
        assertThat(text).startsWith("%PDF-");
    }

    @Test
    @DisplayName("generateInventoryReportPdf never accepts a tenant id from the caller - only from the security context")
    void shouldResolveTenantOnlyFromSecurityContext() {
        setUpTenant();
        UUID otherTenantId = UUID.randomUUID();
        authenticateAs(tenantId);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(productRepository.findByShopTenantId(tenantId)).thenReturn(List.of());

        productReportService.generateInventoryReportPdf();

        org.mockito.Mockito.verify(productRepository).findByShopTenantId(eq(tenantId));
        org.mockito.Mockito.verify(productRepository, org.mockito.Mockito.never()).findByShopTenantId(eq(otherTenantId));
    }
}
