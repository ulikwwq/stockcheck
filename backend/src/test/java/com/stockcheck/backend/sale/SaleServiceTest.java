package com.stockcheck.backend.sale;

import com.stockcheck.backend.audit.AuditLog;
import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.inventory.StockMovement;
import com.stockcheck.backend.inventory.StockMovementRepository;
import com.stockcheck.backend.inventory.StockMovementType;
import com.stockcheck.backend.product.Product;
import com.stockcheck.backend.product.ProductRepository;
import com.stockcheck.backend.sale.dto.CreateSaleItemRequest;
import com.stockcheck.backend.sale.dto.CreateSaleRequest;
import com.stockcheck.backend.sale.dto.SaleResponse;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.shop.ShopRepository;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private SaleService saleService;

    private UUID tenantId;
    private UUID userId;
    private Tenant tenant;
    private Shop shop;
    private User seller;
    private Product product;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();

        tenant = new Tenant("Boutique");
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        shop = new Shop(tenant, "Main Branch", null);
        ReflectionTestUtils.setField(shop, "id", UUID.randomUUID());

        seller = new User(tenant, "aida_seller", "pass", "Aida", "S");
        ReflectionTestUtils.setField(seller, "id", userId);

        product = new Product(
                shop,
                null,
                "Silk Scarf",
                "SCARF-01",
                "Pure silk",
                null,
                new BigDecimal("600.00"),
                new BigDecimal("1000.00"),
                10
        );
        ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String role) {
        StockcheckPrincipal principal = new StockcheckPrincipal(
                userId, tenantId, "aida_seller", "Aida", "S", "pass", true, List.of(role)
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private CreateSaleRequest requestFor(CreateSaleItemRequest... items) {
        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(items));
        return request;
    }

    @Test
    @DisplayName("createSale atomically reduces stock, records sale item with profit, stock movement, and audit log")
    void shouldExecuteSaleSuccessfully() {
        authenticateAs("ADMINISTRATOR");

        when(shopRepository.findByTenantId(tenantId)).thenReturn(List.of(shop));
        when(userRepository.findById(userId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdAndShopTenantIdForUpdate(product.getId(), tenantId)).thenReturn(Optional.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
            return s;
        });

        CreateSaleRequest request = requestFor(new CreateSaleItemRequest(product.getId(), 2, null)); // default sale price 1000

        SaleResponse response = saleService.createSale(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("2000.00");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(response.getItems().get(0).getPurchasePrice()).isEqualByComparingTo("600.00");
        assertThat(response.getItems().get(0).getProfit()).isEqualByComparingTo("800.00"); // (1000 - 600) * 2

        // Stock decreased from 10 to 8
        assertThat(product.getQuantity()).isEqualTo(8);
        verify(productRepository).save(product);

        // Stock movement recorded
        ArgumentCaptor<StockMovement> smCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(smCaptor.capture());
        assertThat(smCaptor.getValue().getQuantityChange()).isEqualTo(-2);
        assertThat(smCaptor.getValue().getType()).isEqualTo(StockMovementType.SALE);

        // Audit log recorded
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo("SALE_CREATED");
    }

    @Test
    @DisplayName("createSale rejects when inventory is insufficient")
    void shouldRejectSaleWhenInsufficientStock() {
        authenticateAs("SELLER");

        product.setQuantity(1);

        when(shopRepository.findByTenantId(tenantId)).thenReturn(List.of(shop));
        when(userRepository.findById(userId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdAndShopTenantIdForUpdate(product.getId(), tenantId)).thenReturn(Optional.of(product));

        CreateSaleRequest request = requestFor(new CreateSaleItemRequest(product.getId(), 5, null));

        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Недостаточно товара");
    }

    @Test
    @DisplayName("createSale rejects a sale that cannot reduce stock below zero even for the last unit")
    void shouldRejectSellingMoreThanLastUnit() {
        authenticateAs("SELLER");
        product.setQuantity(1);

        when(shopRepository.findByTenantId(tenantId)).thenReturn(List.of(shop));
        when(userRepository.findById(userId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdAndShopTenantIdForUpdate(product.getId(), tenantId)).thenReturn(Optional.of(product));

        CreateSaleRequest request = requestFor(new CreateSaleItemRequest(product.getId(), 2, null));

        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(product.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("createSale masks purchasePrice and profit when caller is SELLER")
    void shouldMaskProfitForSeller() {
        authenticateAs("SELLER");

        when(shopRepository.findByTenantId(tenantId)).thenReturn(List.of(shop));
        when(userRepository.findById(userId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdAndShopTenantIdForUpdate(product.getId(), tenantId)).thenReturn(Optional.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
            return s;
        });

        CreateSaleRequest request = requestFor(new CreateSaleItemRequest(product.getId(), 1, new BigDecimal("1200.00")));

        SaleResponse response = saleService.createSale(request);

        assertThat(response.getTotalAmount()).isEqualByComparingTo("1200.00");
        assertThat(response.getItems().get(0).getSalePrice()).isEqualByComparingTo("1200.00");
        assertThat(response.getItems().get(0).getPurchasePrice()).isNull();
        assertThat(response.getItems().get(0).getProfit()).isNull();
    }

    @Test
    @DisplayName("createSale requires an explicit price when the product has no default sale price")
    void shouldRequirePriceWhenNoDefaultSalePrice() {
        authenticateAs("SELLER");
        product.setDefaultSalePrice(null);

        when(shopRepository.findByTenantId(tenantId)).thenReturn(List.of(shop));
        when(userRepository.findById(userId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdAndShopTenantIdForUpdate(product.getId(), tenantId)).thenReturn(Optional.of(product));

        CreateSaleRequest request = requestFor(new CreateSaleItemRequest(product.getId(), 1, null));

        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Укажите цену продажи");
    }
}
