package com.stockcheck.backend.shop;

import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.shop.dto.CreateShopRequest;
import com.stockcheck.backend.shop.dto.ShopResponse;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final TenantRepository tenantRepository;

    public ShopService(ShopRepository shopRepository, TenantRepository tenantRepository) {
        this.shopRepository = shopRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public ShopResponse createShop(CreateShopRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found: " + tenantId));

        Shop shop = shopRepository.save(new Shop(
                tenant,
                request.getName().trim(),
                request.getAddress() != null ? request.getAddress().trim() : null
        ));

        return ShopResponse.fromEntity(shop);
    }

    @Transactional(readOnly = true)
    public List<ShopResponse> getShopsByCurrentTenant() {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        return shopRepository.findByTenantId(tenantId).stream()
                .map(ShopResponse::fromEntity)
                .toList();
    }
}
