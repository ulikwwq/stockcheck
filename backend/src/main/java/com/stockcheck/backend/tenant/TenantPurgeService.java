package com.stockcheck.backend.tenant;

import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.storage.SupabaseStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class TenantPurgeService {

    public static final UUID SYSTEM_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Logger log = LoggerFactory.getLogger(TenantPurgeService.class);

    private static final String SHOPS = "(SELECT id FROM shops WHERE tenant_id = ?)";
    private static final String PRODUCTS = "(SELECT id FROM products WHERE shop_id IN " + SHOPS + ")";
    private static final String USERS = "(SELECT id FROM users WHERE tenant_id = ?)";

    private final JdbcTemplate jdbc;
    private final SupabaseStorageService storage;

    public TenantPurgeService(JdbcTemplate jdbc, SupabaseStorageService storage) {
        this.jdbc = jdbc;
        this.storage = storage;
    }

    /** Физически удаляет бизнес и все его данные одной транзакцией. Доступ: только SUPER_ADMIN (см. контроллер). */
    @Transactional
    public void purge(UUID tenantId) {
        if (SYSTEM_TENANT_ID.equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Системный бизнес удалить нельзя");
        }

        // Блокируем строку: параллельный повторный запрос дождётся и получит 404.
        List<String> names = jdbc.queryForList("SELECT name FROM tenants WHERE id = ? FOR UPDATE", String.class, tenantId);
        if (names.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Бизнес не найден");
        }

        List<String> imagePaths = jdbc.queryForList(
                "SELECT image_path FROM products WHERE shop_id IN " + SHOPS + " AND image_path IS NOT NULL",
                String.class, tenantId);

        jdbc.update("UPDATE tenants SET owner_user_id = NULL WHERE id = ?", tenantId);

        jdbc.update("DELETE FROM sale_items WHERE product_id IN " + PRODUCTS, tenantId);
        jdbc.update("DELETE FROM sales WHERE shop_id IN " + SHOPS, tenantId);          // sale_items уже удалены
        jdbc.update("DELETE FROM stock_movements WHERE product_id IN " + PRODUCTS, tenantId);
        jdbc.update("DELETE FROM stock_movements WHERE user_id IN " + USERS, tenantId);
        jdbc.update("DELETE FROM audit_logs WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM audit_logs WHERE user_id IN " + USERS, tenantId);
        jdbc.update("DELETE FROM expenses WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM expenses WHERE user_id IN " + USERS, tenantId);
        jdbc.update("DELETE FROM products WHERE shop_id IN " + SHOPS, tenantId);
        jdbc.update("DELETE FROM categories WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM shops WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM user_roles WHERE user_id IN " + USERS, tenantId);
        jdbc.update("DELETE FROM users WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM tenants WHERE id = ?", tenantId);

        // След операции храним в системном бизнесе: данные самого бизнеса уже удалены.
        jdbc.update("INSERT INTO audit_logs (tenant_id, user_id, action, entity_type, entity_id, details) VALUES (?,?,?,?,?,?)",
                SYSTEM_TENANT_ID, SecurityUtils.getCurrentUserId().orElse(null),
                "BUSINESS_PURGED", "TENANT", tenantId, names.get(0));

        deleteImagesAfterCommit(imagePaths);
    }

    private void deleteImagesAfterCommit(List<String> paths) {
        if (paths.isEmpty()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String path : paths) {
                    try {
                        storage.delete(path);
                    } catch (RuntimeException e) {
                        log.warn("Не удалось удалить файл {} после удаления бизнеса", path, e);
                    }
                }
            }
        });
    }
}