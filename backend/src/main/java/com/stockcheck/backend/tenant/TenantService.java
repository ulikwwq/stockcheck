package com.stockcheck.backend.tenant;

import com.stockcheck.backend.audit.AuditLog;
import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.role.UserRole;
import com.stockcheck.backend.role.UserRoleRepository;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.shop.ShopRepository;
import com.stockcheck.backend.tenant.dto.ChangeUsernameRequest;
import com.stockcheck.backend.tenant.dto.CreateTenantRequest;
import com.stockcheck.backend.tenant.dto.ResetPasswordRequest;
import com.stockcheck.backend.tenant.dto.TenantResponse;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantService(
            TenantRepository tenantRepository,
            ShopRepository shopRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tenantRepository = tenantRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        String username = request.getOwnerUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Такой логин уже существует");
        }

        Tenant tenant = tenantRepository.save(new Tenant(request.getTenantName().trim()));

        String shopName = StringUtils.hasText(request.getShopName())
                ? request.getShopName().trim()
                : request.getTenantName().trim();
        shopRepository.save(new Shop(tenant, shopName, null));

        String encodedPassword = passwordEncoder.encode(request.getOwnerPassword());
        User owner = userRepository.save(new User(
                tenant,
                username,
                encodedPassword,
                trimToNull(request.getOwnerFirstName()),
                trimToNull(request.getOwnerLastName())
        ));

        userRoleRepository.save(new UserRole(owner, RoleName.ADMINISTRATOR));

        tenant.setOwner(owner);
        tenant = tenantRepository.save(tenant);

        auditLogRepository.save(new AuditLog(
                tenant, owner, "BUSINESS_CREATED", "TENANT", tenant.getId(), tenant.getName()
        ));

        return TenantResponse.fromEntity(tenant);
    }

    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(TenantResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID id) {
        return TenantResponse.fromEntity(findTenantOrThrow(id));
    }

    @Transactional
    public TenantResponse setStatus(UUID id, TenantStatus status) {
        Tenant tenant = findTenantOrThrow(id);
        tenant.setStatus(status);
        tenant = tenantRepository.save(tenant);

        String action = switch (status) {
            case ACTIVE -> "BUSINESS_ACTIVATED";
            case INACTIVE -> "BUSINESS_DEACTIVATED";
            case DELETED -> "BUSINESS_DELETED";
        };

        auditLogRepository.save(new AuditLog(
                tenant, tenant.getOwner(), action, "TENANT", tenant.getId(), tenant.getName()
        ));

        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Soft-deletes a business: sets it to the terminal DELETED status. No
     * rows are removed — products, sales, users and history all stay in
     * place, but the business's users can no longer log in. This is the
     * "Удалить" operation, distinct from "Заблокировать" (deactivate),
     * which is recoverable via activate.
     */
    @Transactional
    public TenantResponse deleteTenant(UUID id) {
        return setStatus(id, TenantStatus.DELETED);
    }

    /**
     * Changes the business owner's username without touching their user ID,
     * tenant, password, or any historical data — used by SUPER_ADMIN to
     * recover an account when the owner has lost/forgotten their login.
     */
    @Transactional
    public TenantResponse changeOwnerUsername(UUID tenantId, ChangeUsernameRequest request) {
        Tenant tenant = findTenantOrThrow(tenantId);
        User owner = tenant.getOwner();
        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "У этого бизнеса нет владельца");
        }

        String newUsername = request.getNewUsername().trim();
        String previousUsername = owner.getUsername();

        if (!newUsername.equalsIgnoreCase(previousUsername) && userRepository.existsByUsername(newUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Такой логин уже существует");
        }

        owner.setUsername(newUsername);
        userRepository.save(owner);

        auditLogRepository.save(new AuditLog(
                tenant,
                owner,
                "OWNER_USERNAME_CHANGED",
                "USER",
                owner.getId(),
                previousUsername + " → " + newUsername
        ));

        return TenantResponse.fromEntity(tenant);
    }

    @Transactional
    public void resetOwnerPassword(UUID tenantId, ResetPasswordRequest request) {
        Tenant tenant = findTenantOrThrow(tenantId);
        User owner = tenant.getOwner();
        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "У этого бизнеса нет владельца");
        }

        owner.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(owner);

        auditLogRepository.save(new AuditLog(
                tenant, owner, "PASSWORD_RESET", "USER", owner.getId(), owner.getUsername()
        ));
    }

    private Tenant findTenantOrThrow(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found: " + id));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
