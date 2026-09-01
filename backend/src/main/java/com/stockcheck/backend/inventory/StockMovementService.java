package com.stockcheck.backend.inventory;

import com.stockcheck.backend.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovements(UUID productId) {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        if (productId != null) {
            return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                    .map(StockMovementResponse::fromEntity)
                    .toList();
        }

        return stockMovementRepository.findByTenantId(tenantId).stream()
                .map(StockMovementResponse::fromEntity)
                .toList();
    }
}
