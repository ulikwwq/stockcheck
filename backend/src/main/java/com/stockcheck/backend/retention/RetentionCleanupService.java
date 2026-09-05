package com.stockcheck.backend.retention;

import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.inventory.StockMovementRepository;
import com.stockcheck.backend.sale.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Enforces the platform's 30-day operational data retention policy.
 *
 * <p>Only temporary/history data is removed: sales (and, via DB cascade,
 * their sale items), stock movements, and audit log entries older than 30
 * days. Active business data — products, users, businesses, current
 * inventory — is never touched here.
 */
@Component
public class RetentionCleanupService {

    private static final Logger log = LoggerFactory.getLogger(RetentionCleanupService.class);
    private static final int RETENTION_DAYS = 30;

    private final SaleRepository saleRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AuditLogRepository auditLogRepository;

    public RetentionCleanupService(
            SaleRepository saleRepository,
            StockMovementRepository stockMovementRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.saleRepository = saleRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /** Runs once a day at 03:00 server time — low-traffic hours for a small business app. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldData() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);

        int deletedSales = saleRepository.deleteByCreatedAtBefore(cutoff);
        int deletedMovements = stockMovementRepository.deleteByCreatedAtBefore(cutoff);
        int deletedAuditLogs = auditLogRepository.deleteByCreatedAtBefore(cutoff);

        log.info(
                "Retention cleanup complete: removed {} sales (+items), {} stock movements, {} audit logs older than {}",
                deletedSales, deletedMovements, deletedAuditLogs, cutoff
        );
    }
}
