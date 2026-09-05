package com.stockcheck.backend.shop;

import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.shop.dto.CreateShopRequest;
import com.stockcheck.backend.shop.dto.ShopResponse;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private ShopService shopService;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant("Alpha Market");
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        StockcheckPrincipal principal = new StockcheckPrincipal(
                UUID.randomUUID(),
                tenantId,
                "+79991112233",
                "Admin",
                "User",
                "pass",
                true,
                List.of("ADMINISTRATOR")
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("createShop creates and returns a new shop for the authenticated tenant")
    void shouldCreateShopForCurrentTenant() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(shopRepository.save(any(Shop.class))).thenAnswer(inv -> {
            Shop s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
            return s;
        });

        CreateShopRequest request = new CreateShopRequest("Branch 2", "Main Ave 123");
        ShopResponse response = shopService.createShop(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Branch 2");
        assertThat(response.getAddress()).isEqualTo("Main Ave 123");
        assertThat(response.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("getShopsByCurrentTenant returns list of shops for authenticated tenant")
    void shouldReturnShopsForCurrentTenant() {
        Shop shop = new Shop(tenant, "Branch 1", "Street 1");
        ReflectionTestUtils.setField(shop, "id", UUID.randomUUID());

        when(shopRepository.findByTenantId(tenantId)).thenReturn(List.of(shop));

        List<ShopResponse> shops = shopService.getShopsByCurrentTenant();

        assertThat(shops).hasSize(1);
        assertThat(shops.get(0).getName()).isEqualTo("Branch 1");
        assertThat(shops.get(0).getTenantId()).isEqualTo(tenantId);
    }
}
