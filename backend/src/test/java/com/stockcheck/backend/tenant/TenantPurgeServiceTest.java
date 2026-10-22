package com.stockcheck.backend.tenant;

import com.stockcheck.backend.product.Product;
import com.stockcheck.backend.product.ProductRepository;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.shop.ShopRepository;
import com.stockcheck.backend.storage.SupabaseStorageService;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TenantPurgeService.class)
class TenantPurgeServiceTest {

    @Autowired
    private TenantPurgeService purgeService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private SupabaseStorageService storage;

    @Test
    void purgeRemovesTenantAndAllItsDataButNotOthers() {
        Tenant tenantA = tenantRepository.saveAndFlush(new Tenant("A"));
        Tenant tenantB = tenantRepository.saveAndFlush(new Tenant("B"));

        User ownerA = userRepository.saveAndFlush(
                new User(tenantA, "purge_owner_a", "h", null, null)
        );

        tenantA.setOwner(ownerA);
        tenantRepository.saveAndFlush(tenantA);

        Shop shopA = shopRepository.saveAndFlush(
                new Shop(tenantA, "S", null)
        );

        productRepository.saveAndFlush(
                new Product(shopA, null, "P", null, null, null, null, null, 1)
        );

        User userB = userRepository.saveAndFlush(
                new User(tenantB, "purge_user_b", "h", null, null)
        );

        UUID tenantAId = tenantA.getId();
        UUID tenantBId = tenantB.getId();
        UUID shopAId = shopA.getId();
        UUID userBId = userB.getId();

        purgeService.purge(tenantAId);

        assertThat(count("tenants", "id", tenantAId)).isZero();
        assertThat(count("users", "tenant_id", tenantAId)).isZero();
        assertThat(count("shops", "tenant_id", tenantAId)).isZero();

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM products WHERE shop_id = ?",
                Integer.class,
                shopAId
        )).isZero();

        assertThat(count("tenants", "id", tenantBId)).isEqualTo(1);
        assertThat(userRepository.findById(userBId)).isPresent();

        assertThatThrownBy(() -> purgeService.purge(tenantAId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void systemTenantCannotBeDeleted() {
        assertThatThrownBy(
                () -> purgeService.purge(TenantPurgeService.SYSTEM_TENANT_ID)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Системный");

        assertThat(count(
                "tenants",
                "id",
                TenantPurgeService.SYSTEM_TENANT_ID
        )).isEqualTo(1);
    }

    private int count(String table, String column, UUID id) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?",
                Integer.class,
                id
        );
    }
}