package com.stockcheck.backend.user;

import com.stockcheck.backend.audit.AuditLog;
import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.auth.dto.UserResponse;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.role.UserRole;
import com.stockcheck.backend.role.UserRoleRepository;
import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import com.stockcheck.backend.user.dto.CreateUserRequest;
import com.stockcheck.backend.user.dto.UpdateUserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Manages SELLER accounts on behalf of a business ADMINISTRATOR.
 *
 * <p>Sellers automatically receive the {@link RoleName#SELLER} role only —
 * the administrator does not choose or configure roles/permissions.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TenantRepository tenantRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            TenantRepository tenantRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.tenantRepository = tenantRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createSeller(CreateUserRequest request) {
        UUID tenantId = currentTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found: " + tenantId));

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Такой логин уже существует");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userRepository.save(new User(
                tenant,
                username,
                encodedPassword,
                trimToNull(request.getFirstName()),
                trimToNull(request.getLastName())
        ));

        userRoleRepository.save(new UserRole(user, RoleName.SELLER));

        auditLogRepository.save(new AuditLog(
                tenant, currentActor(user), "SELLER_CREATED", "USER", user.getId(), user.getUsername()
        ));

        return UserResponse.fromUser(user, List.of(RoleName.SELLER));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getSellersByCurrentTenant() {
        UUID tenantId = currentTenantId();

        return userRepository.findByTenantId(tenantId).stream()
                .map(u -> {
                    List<RoleName> roles = userRoleRepository.findByUserId(u.getId()).stream()
                            .map(UserRole::getRole)
                            .toList();
                    return UserResponse.fromUser(u, roles);
                })
                .filter(response -> response.getRoles().contains(RoleName.SELLER))
                .toList();
    }

    @Transactional
    public UserResponse updateSeller(UUID sellerId, UpdateUserRequest request) {
        User seller = findSellerInCurrentTenant(sellerId);

        if (request.getFirstName() != null) {
            seller.setFirstName(trimToNull(request.getFirstName()));
        }
        if (request.getLastName() != null) {
            seller.setLastName(trimToNull(request.getLastName()));
        }
        if (StringUtils.hasText(request.getNewPassword())) {
            seller.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            auditLogRepository.save(new AuditLog(
                    seller.getTenant(), currentActor(seller), "PASSWORD_RESET", "USER", seller.getId(), seller.getUsername()
            ));
        }

        User saved = userRepository.save(seller);
        List<RoleName> roles = userRoleRepository.findByUserId(saved.getId()).stream()
                .map(UserRole::getRole)
                .toList();
        return UserResponse.fromUser(saved, roles);
    }

    @Transactional
    public UserResponse setSellerActive(UUID sellerId, boolean active) {
        User seller = findSellerInCurrentTenant(sellerId);
        seller.setActive(active);
        User saved = userRepository.save(seller);

        auditLogRepository.save(new AuditLog(
                seller.getTenant(),
                currentActor(seller),
                active ? "SELLER_ACTIVATED" : "SELLER_DEACTIVATED",
                "USER",
                seller.getId(),
                seller.getUsername()
        ));

        List<RoleName> roles = userRoleRepository.findByUserId(saved.getId()).stream()
                .map(UserRole::getRole)
                .toList();
        return UserResponse.fromUser(saved, roles);
    }

    private User findSellerInCurrentTenant(UUID sellerId) {
        UUID tenantId = currentTenantId();
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found: " + sellerId));

        if (seller.getTenant() == null || !seller.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller does not belong to your business");
        }

        boolean isSeller = userRoleRepository.findByUserId(seller.getId()).stream()
                .anyMatch(r -> r.getRole() == RoleName.SELLER);
        if (!isSeller) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found: " + sellerId);
        }

        return seller;
    }

    private UUID currentTenantId() {
        return SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));
    }

    private User currentActor(User fallback) {
        return SecurityUtils.getCurrentUserId()
                .flatMap(userRepository::findById)
                .orElse(fallback);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
