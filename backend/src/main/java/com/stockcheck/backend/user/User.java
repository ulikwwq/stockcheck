package com.stockcheck.backend.user;

import com.stockcheck.backend.common.BaseEntity;
import com.stockcheck.backend.tenant.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * A User belongs to exactly one {@link Tenant}.
 *
 * <p>The username is the login identifier and is unique GLOBALLY across the
 * platform (not just within a tenant) — enforced both at the database level
 * and here via the entity's unique constraint declaration. This keeps login
 * to a single "username + password" form with no business selector.
 *
 * <p>Passwords are never stored in plain text; only {@code passwordHash} is
 * persisted.
 *
 * <p>{@code firstName} and {@code lastName} are optional: a business owner
 * or seller may be created with only a username and password.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_users_username",
                columnNames = {"username"}
        )
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected User() {
        // required by JPA
    }

    public User(Tenant tenant, String username, String passwordHash, String firstName, String lastName) {
        this.tenant = tenant;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    /**
     * Display name for lists/history: "First Last" when either name is
     * present, falling back to the username when both are absent (both are
     * optional per spec, so this must never render as the literal "null null").
     */
    public String getDisplayName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
