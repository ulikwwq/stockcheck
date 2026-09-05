package com.stockcheck.backend.auth.dto;

import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.user.User;

import java.util.List;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private UUID tenantId;
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private List<RoleName> roles;

    public UserResponse() {
    }

    public UserResponse(
            UUID id,
            UUID tenantId,
            String username,
            String firstName,
            String lastName,
            boolean active,
            List<RoleName> roles
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
        this.roles = roles;
    }

    public static UserResponse fromUser(User user, List<RoleName> roles) {
        return new UserResponse(
                user.getId(),
                user.getTenant() != null ? user.getTenant().getId() : null,
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                roles
        );
    }

    public static UserResponse fromPrincipal(StockcheckPrincipal principal) {
        List<RoleName> roleNames = principal.getAuthorities().stream()
                .map(auth -> {
                    String name = auth.getAuthority().replace("ROLE_", "");
                    return RoleName.valueOf(name);
                })
                .toList();

        return new UserResponse(
                principal.getUserId(),
                principal.getTenantId(),
                principal.getUsername(),
                principal.getFirstName(),
                principal.getLastName(),
                principal.isEnabled(),
                roleNames
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<RoleName> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleName> roles) {
        this.roles = roles;
    }
}
