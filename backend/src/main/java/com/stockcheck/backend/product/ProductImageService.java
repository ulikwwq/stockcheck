package com.stockcheck.backend.product;

import com.stockcheck.backend.audit.AuditLog;
import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.storage.SupabaseStorageService;
import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

/**
 * Handles product photo upload/replace/delete.
 *
 * <p>Kept as a separate class from {@link ProductService} because it has a
 * genuinely different concern (binary content + external storage calls),
 * but it deliberately reuses the exact same tenant-resolution and
 * ownership-check pattern as ProductService
 * ({@code findByIdAndShopTenantId}) - that repository method is the single
 * gate that makes cross-tenant access impossible, and every method here
 * goes through it before touching storage.
 */
@Service
public class ProductImageService {

    /**
     * 8 MB: comfortably covers a typical modern phone camera JPEG (usually
     * 2-6 MB) with headroom, while still bounding worst-case abuse. Spring's
     * own multipart limit (application.yaml) is set slightly above this so
     * oversized uploads always hit this check and get a Russian message,
     * rather than a generic container-level rejection.
     */
    private static final long MAX_IMAGE_BYTES = 8L * 1024 * 1024;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final SupabaseStorageService storageService;

    public ProductImageService(
            ProductRepository productRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository,
            SupabaseStorageService storageService
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.storageService = storageService;
    }

    /**
     * Uploads a new photo for the product, replacing any existing one.
     * Order of operations: validate -> upload new -> save DB reference ->
     * best-effort delete of the old file. This way a failed upload never
     * touches the DB, and a failed old-file cleanup never breaks the
     * (already successful) new photo.
     */
    @Transactional
    public Product uploadImage(UUID productId, MultipartFile file) {
        Product product = findOwnedProduct(productId);
        DetectedImage detected = validateAndDetect(file);

        String oldPath = product.getImagePath();
        String newPath = buildStoragePath(product, detected.extension());

        storageService.upload(newPath, detected.content(), detected.contentType());

        product.setImagePath(newPath);
        Product saved = productRepository.save(product);

        if (oldPath != null && !oldPath.equals(newPath)) {
            storageService.delete(oldPath);
        }

        auditLogRepository.save(new AuditLog(
                product.getShop().getTenant(),
                currentUser(),
                "PRODUCT_IMAGE_UPDATED",
                "PRODUCT",
                saved.getId(),
                saved.getName(),
                oldPath == null ? "Без фото" : "Фото",
                "Фото"
        ));

        return saved;
    }

    /** Removes the product's photo, if any. Idempotent. */
    @Transactional
    public Product deleteImage(UUID productId) {
        Product product = findOwnedProduct(productId);
        String oldPath = product.getImagePath();

        if (oldPath == null) {
            return product;
        }

        product.setImagePath(null);
        Product saved = productRepository.save(product);
        storageService.delete(oldPath);

        auditLogRepository.save(new AuditLog(
                product.getShop().getTenant(),
                currentUser(),
                "PRODUCT_IMAGE_DELETED",
                "PRODUCT",
                saved.getId(),
                saved.getName(),
                "Фото",
                "Без фото"
        ));
        return saved;
    }

    /**
     * Resolves the product scoped to the current tenant - the same
     * repository method ProductService uses for every other mutation. A
     * product ID from another tenant simply does not exist from this
     * query's point of view, so it 404s rather than ever leaking whether
     * the ID belongs to someone else.
     */
    private Product findOwnedProduct(UUID productId) {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        return productRepository.findByIdAndShopTenantId(productId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
    }

    /**
     * The tenant and product segments come only from the already-resolved,
     * tenant-scoped {@code product} entity - never from client input - so a
     * caller cannot manipulate which tenant's folder an image lands in. The
     * filename itself is a fresh random UUID, so replacing an image can
     * never collide with or overwrite a stale cached copy of the old one.
     */
    private String buildStoragePath(Product product, String extension) {
        UUID tenantId = product.getShop().getTenant().getId();
        return "tenants/" + tenantId + "/products/" + product.getId() + "/" + UUID.randomUUID() + "." + extension;
    }

    private DetectedImage validateAndDetect(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Выберите файл изображения");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Размер изображения не должен превышать 8 МБ");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось прочитать файл");
        }

        // Never trust the client-declared MIME type or filename extension -
        // sniff the actual file signature (magic bytes) instead.
        DetectedImage detected = sniffImageType(content);
        if (detected == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Поддерживаются только изображения формата JPEG, PNG или WebP"
            );
        }
        return detected;
    }

    private record DetectedImage(byte[] content, String contentType, String extension) {
    }

    private static DetectedImage sniffImageType(byte[] bytes) {
        if (matches(bytes, 0, JPEG_MAGIC)) {
            return new DetectedImage(bytes, "image/jpeg", "jpg");
        }
        if (matches(bytes, 0, PNG_MAGIC)) {
            return new DetectedImage(bytes, "image/png", "png");
        }
        if (bytes.length >= 12
                && matches(bytes, 0, RIFF_MAGIC)
                && matches(bytes, 8, WEBP_MAGIC)) {
            return new DetectedImage(bytes, "image/webp", "webp");
        }
        return null;
    }

    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_MAGIC = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP_MAGIC = {'W', 'E', 'B', 'P'};

    private static boolean matches(byte[] data, int offset, byte[] magic) {
        if (data.length < offset + magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (data[offset + i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private User currentUser() {
        return SecurityUtils.getCurrentUserId().flatMap(userRepository::findById).orElse(null);
    }
}
