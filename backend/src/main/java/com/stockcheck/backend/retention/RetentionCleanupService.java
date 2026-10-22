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
 * Enforces the platform's data retention policy.
 *
 * <p>Sales and stock movements are retained for 13 months to support
 * 12 months of history with an additional buffer.
 * Audit logs are retained for 30 days.
 *
 * <p>Active business data — products, users, businesses, and current
 * inventory — is not removed by this service.
 */
@Component
public class RetentionCleanupService {

    private static final Logger log =
            LoggerFactory.getLogger(RetentionCleanupService.class);

    private static final int AUDIT_RETENTION_DAYS = 30;
    private static final int SALES_RETENTION_MONTHS = 13;

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

    /**
     * Runs once a day at 03:00 server time.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldData() {
        LocalDateTime salesCutoff =
                LocalDateTime.now().minusMonths(SALES_RETENTION_MONTHS);

        LocalDateTime auditCutoff =
                LocalDateTime.now().minusDays(AUDIT_RETENTION_DAYS);

        int deletedSales =
                saleRepository.deleteByCreatedAtBefore(salesCutoff);

        int deletedMovements =
                stockMovementRepository.deleteByCreatedAtBefore(salesCutoff);

        int deletedAuditLogs =
                auditLogRepository.deleteByCreatedAtBefore(auditCutoff);

        log.info(
                "Retention cleanup complete: removed {} sales (+items), " +
                        "{} stock movements older than {}, " +
                        "{} audit logs older than {}",
                deletedSales,
                deletedMovements,
                salesCutoff,
                deletedAuditLogs,
                auditCutoff
        );
    }
}