package com.stockcheck.backend.shop;

import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShopRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Test
    void shopBelongsToATenant() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Azamat Store"));

        Shop shop = shopRepository.saveAndFlush(
                new Shop(tenant, "Main Branch", "123 Chuy Ave")
        );

        assertNotNull(shop.getId());
        assertEquals(tenant.getId(), shop.getTenant().getId());

        List<Shop> shopsOfTenant = shopRepository.findByTenantId(tenant.getId());
        assertEquals(1, shopsOfTenant.size());
    }

    @Test
    void aTenantCanHaveMultipleShops() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Nurbek Store"));

        shopRepository.saveAndFlush(new Shop(tenant, "Branch 1", "Address 1"));
        shopRepository.saveAndFlush(new Shop(tenant, "Branch 2", "Address 2"));

        List<Shop> shopsOfTenant = shopRepository.findByTenantId(tenant.getId());
        assertEquals(2, shopsOfTenant.size());
    }
}
