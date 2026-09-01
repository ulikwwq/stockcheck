package com.stockcheck.backend.product;

import com.stockcheck.backend.category.Category;
import com.stockcheck.backend.category.CategoryRepository;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.shop.ShopRepository;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("product persists with shop, category, prices, and quantity")
    void shouldPersistAndRetrieveProduct() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Apparel Shop"));
        Shop shop = shopRepository.saveAndFlush(new Shop(tenant, "Main Branch", null));
        Category category = categoryRepository.saveAndFlush(new Category(tenant, "T-Shirts"));

        Product product = new Product(
                shop,
                category,
                "Cotton T-Shirt Black",
                "TSH-BLK-001",
                "100% organic cotton",
                "https://example.com/tshirt.png",
                new BigDecimal("500.00"),
                new BigDecimal("990.00"),
                25
        );

        Product saved = productRepository.saveAndFlush(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getShop().getId()).isEqualTo(shop.getId());
        assertThat(saved.getCategory().getId()).isEqualTo(category.getId());
        assertThat(saved.getPurchasePrice()).isEqualByComparingTo("500.00");
        assertThat(saved.getDefaultSalePrice()).isEqualByComparingTo("990.00");
        assertThat(saved.getQuantity()).isEqualTo(25);

        List<Product> byShop = productRepository.findByShopId(shop.getId());
        assertThat(byShop).hasSize(1);

        List<Product> byTenant = productRepository.findByShopTenantId(tenant.getId());
        assertThat(byTenant).hasSize(1);

        Optional<Product> byIdAndTenant = productRepository.findByIdAndShopTenantId(saved.getId(), tenant.getId());
        assertThat(byIdAndTenant).isPresent();
    }
}
