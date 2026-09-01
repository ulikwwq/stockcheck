package com.stockcheck.backend.product;

import com.stockcheck.backend.audit.AuditLog;
import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.category.Category;
import com.stockcheck.backend.category.CategoryRepository;
import com.stockcheck.backend.product.dto.CreateProductRequest;
import com.stockcheck.backend.product.dto.ProductResponse;
import com.stockcheck.backend.product.dto.UpdateProductRequest;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.shop.ShopRepository;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ProductService(
            ProductRepository productRepository,
            ShopRepository shopRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        UUID tenantId = currentTenantId();
        Shop shop = resolveTenantShop(tenantId);

        Category category = resolveCategory(request.getCategoryId(), tenantId);

        Product product = new Product(
                shop,
                category,
                request.getName().trim(),
                request.getSku() != null ? request.getSku().trim() : null,
                request.getDescription(),
                request.getImageUrl(),
                request.getPurchasePrice(),
                request.getDefaultSalePrice(),
                request.getQuantity()
        );

        Product saved = productRepository.save(product);

        auditLogRepository.save(new AuditLog(
                shop.getTenant(), currentUser(), "PRODUCT_CREATED", "PRODUCT", saved.getId(), saved.getName()
        ));

        return ProductResponse.fromEntity(saved, true);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        UUID tenantId = currentTenantId();

        Product product = productRepository.findByIdAndShopTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));

        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName().trim());
        }
        if (request.getSku() != null) {
            product.setSku(request.getSku().trim());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getPurchasePrice() != null) {
            product.setPurchasePrice(request.getPurchasePrice());
        }
        if (request.getDefaultSalePrice() != null) {
            product.setDefaultSalePrice(request.getDefaultSalePrice());
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }

        boolean wasActive = product.isActive();
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        if (request.getCategoryId() != null) {
            product.setCategory(resolveCategory(request.getCategoryId(), tenantId));
        }

        Product updated = productRepository.save(product);

        if (request.getActive() != null && wasActive != request.getActive()) {
            auditLogRepository.save(new AuditLog(
                    product.getShop().getTenant(),
                    currentUser(),
                    request.getActive() ? "PRODUCT_ACTIVATED" : "PRODUCT_DELETED",
                    "PRODUCT",
                    updated.getId(),
                    updated.getName()
            ));
        }

        return ProductResponse.fromEntity(updated, true);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(UUID shopId) {
        UUID tenantId = currentTenantId();
        boolean canViewSensitiveInfo = canViewSensitiveInfo();

        if (shopId != null) {
            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found: " + shopId));

            if (!shop.getTenant().getId().equals(tenantId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Shop does not belong to your business");
            }

            return productRepository.findByShopId(shopId).stream()
                    .filter(Product::isActive)
                    .map(p -> ProductResponse.fromEntity(p, canViewSensitiveInfo))
                    .toList();
        }

        return productRepository.findByShopTenantId(tenantId).stream()
                .filter(Product::isActive)
                .map(p -> ProductResponse.fromEntity(p, canViewSensitiveInfo))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        UUID tenantId = currentTenantId();

        Product product = productRepository.findByIdAndShopTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));

        return ProductResponse.fromEntity(product, canViewSensitiveInfo());
    }

    /**
     * The mobile UI never asks the administrator to pick a shop — a business
     * has exactly one operating shop in this simplified product. Resolve it
     * automatically instead of requiring a shopId in every request.
     */
    private Shop resolveTenantShop(UUID tenantId) {
        List<Shop> shops = shopRepository.findByTenantId(tenantId);
        if (shops.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "У этого бизнеса нет магазина");
        }
        return shops.get(0);
    }

    private Category resolveCategory(UUID categoryId, UUID tenantId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + categoryId));

        if (!category.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Category does not belong to your business");
        }
        return category;
    }

    private UUID currentTenantId() {
        return SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));
    }

    private User currentUser() {
        return SecurityUtils.getCurrentUserId().flatMap(userRepository::findById).orElse(null);
    }

    private boolean canViewSensitiveInfo() {
        return SecurityUtils.hasRole(RoleName.ADMINISTRATOR) || SecurityUtils.hasRole(RoleName.SUPER_ADMIN);
    }
}
