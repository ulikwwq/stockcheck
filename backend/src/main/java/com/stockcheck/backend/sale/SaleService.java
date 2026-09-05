package com.stockcheck.backend.sale;

import com.stockcheck.backend.audit.AuditLog;
import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.inventory.StockMovement;
import com.stockcheck.backend.inventory.StockMovementRepository;
import com.stockcheck.backend.inventory.StockMovementType;
import com.stockcheck.backend.product.Product;
import com.stockcheck.backend.product.ProductRepository;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.sale.dto.CreateSaleItemRequest;
import com.stockcheck.backend.sale.dto.CreateSaleRequest;
import com.stockcheck.backend.sale.dto.SaleResponse;
import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.shop.ShopRepository;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AuditLogRepository auditLogRepository;

    public SaleService(
            SaleRepository saleRepository,
            ShopRepository shopRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            StockMovementRepository stockMovementRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.saleRepository = saleRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Selling and the resulting stock decrement happen inside a single
     * transaction: either the whole sale (all items + inventory updates)
     * commits, or none of it does. Stock is re-checked against the current
     * database row (not a stale in-memory copy) immediately before the
     * decrement, so concurrent sales of the last unit cannot both succeed.
     */
    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user context is required"));

        List<Shop> shops = shopRepository.findByTenantId(tenantId);
        if (shops.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "У этого бизнеса нет магазина");
        }
        Shop shop = shops.get(0);

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller user not found: " + userId));

        Sale sale = new Sale(shop, seller);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateSaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndShopTenantIdForUpdate(itemReq.getProductId(), tenantId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Товар не найден"
                    ));

            if (!product.isActive()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден");
            }

            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Недостаточно товара на складе"
                );
            }

            BigDecimal salePrice = itemReq.getCustomSalePrice() != null
                    ? itemReq.getCustomSalePrice()
                    : product.getDefaultSalePrice();

            // salePrice may legitimately be null here: the product has no
            // configured default price and the seller chose not to specify
            // one for this sale. That is allowed - do not invent a price
            // such as 0; record it as genuinely unknown instead.

            BigDecimal purchasePrice = product.getPurchasePrice();

            // Decrement inventory atomically with the rest of the sale.
            product.setQuantity(product.getQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            SaleItem saleItem = new SaleItem(product, itemReq.getQuantity(), purchasePrice, salePrice);
            sale.addItem(saleItem);

            if (salePrice != null) {
                totalAmount = totalAmount.add(salePrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            }

            stockMovementRepository.save(new StockMovement(
                    product,
                    StockMovementType.SALE,
                    -itemReq.getQuantity(),
                    seller
            ));
        }

        sale.setTotalAmount(totalAmount);
        Sale savedSale = saleRepository.save(sale);

        auditLogRepository.save(new AuditLog(
                shop.getTenant(),
                seller,
                "SALE_CREATED",
                "SALE",
                savedSale.getId(),
                totalAmount.toPlainString()
        ));

        boolean canViewSensitive = canViewSensitiveInfo();
        return SaleResponse.fromEntity(savedSale, canViewSensitive);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getSales(UUID shopId) {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        boolean canViewSensitive = canViewSensitiveInfo();

        if (shopId != null) {
            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found: " + shopId));

            if (!shop.getTenant().getId().equals(tenantId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Shop does not belong to your business");
            }

            return saleRepository.findByShopIdOrderByCreatedAtDesc(shopId).stream()
                    .map(s -> SaleResponse.fromEntity(s, canViewSensitive))
                    .toList();
        }

        return saleRepository.findByTenantId(tenantId).stream()
                .map(s -> SaleResponse.fromEntity(s, canViewSensitive))
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(UUID id) {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        Sale sale = saleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found: " + id));

        return SaleResponse.fromEntity(sale, canViewSensitiveInfo());
    }

    private boolean canViewSensitiveInfo() {
        return SecurityUtils.hasRole(RoleName.ADMINISTRATOR) || SecurityUtils.hasRole(RoleName.SUPER_ADMIN);
    }
}
