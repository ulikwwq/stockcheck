package com.stockcheck.backend.product;

import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.category.Category;
import com.stockcheck.backend.category.CategoryRepository;
import com.stockcheck.backend.product.dto.CreateProductRequest;
import com.stockcheck.backend.product.dto.ProductResponse;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.shop.ShopRepository;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ProductService productService;

    private UUID tenantId;
    private Tenant tenant;
    private Shop shop;
    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant("Sports Store");
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        shop = new Shop(tenant, "Downtown Shop", null);
        ReflectionTestUtils.setField(shop, "id", UUID.randomUUID());

        category = new Category(tenant, "Footwear");
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());

        product = new Product(
                shop,
                category,
                "Running Shoes",
                "SH-RUN-01",
                "Comfortable shoes",
                "https://example.com/shoes.png",
                new BigDecimal("3000.00"),
                new BigDecimal("5500.00"),
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
                UUID.randomUUID(), tenantId, "test_user", "User", "Test", "pass", true, List.of(role)
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        lenient().when(userRepository.findById(principal.getUserId())).thenReturn(Optional.empty());
    }

    private CreateProductRequest buildRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Running Shoes");
        request.setSku("SH-RUN-01");
        request.setDescription("Comfortable shoes");
        request.setImageUrl("https://example.com/shoes.png");
        request.setPurchasePrice(new BigDecimal("3000.00"));
        request.setDefaultSalePrice(new BigDecimal("5500.00"));
        request.setQuantity(10);
        request.setCategoryId(category.getId());
        return request;
    }

    @Test
    @DisplayName("createProduct auto-resolves the tenant's shop and creates the product")
    void shouldCreateProductSuccessfully() {
        authenticateAs("ADMINISTRATOR");

        when(shopRepository.findByTenantId(tenantId)).thenReturn(List.of(shop));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
            return p;
        });

        ProductResponse response = productService.createProduct(buildRequest());

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Running Shoes");
        assertThat(response.getPurchasePrice()).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("createProduct only requires name and quantity - all other fields are optional")
    void shouldCreateProductWithOnlyRequiredFields() {
        authenticateAs("ADMINISTRATOR");

        when(shopRepository.findByTenantId(tenantId)).thenReturn(List.of(shop));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
            return p;
        });

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Кофе");
        request.setQuantity(20);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getPurchasePrice()).isNull();
        assertThat(response.getDefaultSalePrice()).isNull();
    }

    @Test
    @DisplayName("createProduct throws 404 when the business has no shop configured")
    void shouldFailWhenNoShopExists() {
        authenticateAs("ADMINISTRATOR");
        when(shopRepository.findByTenantId(tenantId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> productService.createProduct(buildRequest()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("getProducts includes purchasePrice for ADMINISTRATOR")
    void shouldIncludePurchasePriceForAdministrator() {
        authenticateAs("ADMINISTRATOR");

        when(productRepository.findByShopTenantId(tenantId)).thenReturn(List.of(product));

        List<ProductResponse> products = productService.getProducts(null);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getPurchasePrice()).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("getProducts masks purchasePrice (sets null) for SELLER")
    void shouldMaskPurchasePriceForSeller() {
        authenticateAs("SELLER");

        when(productRepository.findByShopTenantId(tenantId)).thenReturn(List.of(product));

        List<ProductResponse> products = productService.getProducts(null);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Running Shoes");
        assertThat(products.get(0).getDefaultSalePrice()).isEqualByComparingTo("5500.00");
        assertThat(products.get(0).getPurchasePrice()).isNull();
    }

    @Test
    @DisplayName("getProducts excludes deactivated (deleted) products")
    void shouldExcludeInactiveProducts() {
        authenticateAs("ADMINISTRATOR");
        product.setActive(false);

        when(productRepository.findByShopTenantId(tenantId)).thenReturn(List.of(product));

        List<ProductResponse> products = productService.getProducts(null);

        assertThat(products).isEmpty();
    }
}
