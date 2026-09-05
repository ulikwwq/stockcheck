package com.stockcheck.backend.product;

import com.stockcheck.backend.category.Category;
import com.stockcheck.backend.common.BaseEntity;
import com.stockcheck.backend.shop.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "default_sale_price", precision = 12, scale = 2)
    private BigDecimal defaultSalePrice;

    @Column(name = "quantity", nullable = false)
    private int quantity = 0;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected Product() {
        // JPA
    }

    public Product(
            Shop shop,
            Category category,
            String name,
            String sku,
            String description,
            String imageUrl,
            BigDecimal purchasePrice,
            BigDecimal defaultSalePrice,
            int quantity
    ) {
        this.shop = shop;
        this.category = category;
        this.name = name;
        this.sku = sku;
        this.description = description;
        this.imageUrl = imageUrl;
        this.purchasePrice = purchasePrice;
        this.defaultSalePrice = defaultSalePrice;
        this.quantity = quantity;
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getDefaultSalePrice() {
        return defaultSalePrice;
    }

    public void setDefaultSalePrice(BigDecimal defaultSalePrice) {
        this.defaultSalePrice = defaultSalePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
