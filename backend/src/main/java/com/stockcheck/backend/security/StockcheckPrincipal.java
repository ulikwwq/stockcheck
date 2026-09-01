package com.stockcheck.backend.security;

import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Custom {@link UserDetails} principal representing an authenticated StockCheck user.
 *
 * <p>Holds the user's primary identity as well as the tenant context ({@code tenantId}),
 * ensuring controllers and services can access tenant information directly from the
 * security context without hitting the database.
 */
public class StockcheckPrincipal implements UserDetails {

    private final UUID userId;
    private final UUID tenantId;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String passwordHash;
    private final boolean active;
    private final Set<GrantedAuthority> authorities;

    public StockcheckPrincipal(
            UUID userId,
            UUID tenantId,
            String username,
            String firstName,
            String lastName,
            String passwordHash,
            boolean active,
            Collection<String> roleNames
    ) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.active = active;
        this.authorities = (roleNames == null || roleNames.isEmpty())
                ? Collections.emptySet()
                : roleNames.stream()
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static StockcheckPrincipal fromUser(User user, List<RoleName> roles) {
        List<String> roleStrings = (roles == null)
                ? Collections.emptyList()
                : roles.stream().map(Enum::name).toList();

        return new StockcheckPrincipal(
                user.getId(),
                user.getTenant() != null ? user.getTenant().getId() : null,
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPasswordHash(),
                user.isActive(),
                roleStrings
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
