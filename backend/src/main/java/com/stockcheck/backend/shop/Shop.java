package com.stockcheck.backend.shop;

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

import java.util.UUID;

/**
 * A Shop is a physical or logical point of sale belonging to exactly one
 * {@link Tenant}. A Tenant may have one or more shops.
 */
@Entity
@Table(name = "shops")
public class Shop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected Shop() {
        // required by JPA
    }

    public Shop(Tenant tenant, String name, String address) {
        this.tenant = tenant;
        this.name = name;
        this.address = address;
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
