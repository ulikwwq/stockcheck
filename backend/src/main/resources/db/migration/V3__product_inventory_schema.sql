-- Phase 2, Part 2: Product & Category domain with multi-tenancy and inventory constraints.
-- Drop placeholder Phase 1 tables if they exist

DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id  UUID NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_categories_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),

    CONSTRAINT uq_categories_tenant_name
        UNIQUE (tenant_id, name)
);

CREATE INDEX idx_categories_tenant_id ON categories (tenant_id);

CREATE TABLE products (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id            UUID NOT NULL,
    category_id        UUID,
    name               VARCHAR(255) NOT NULL,
    sku                VARCHAR(100),
    description        TEXT,
    image_url          VARCHAR(1000),
    purchase_price     NUMERIC(12, 2) NOT NULL,
    default_sale_price NUMERIC(12, 2) NOT NULL,
    quantity           INT NOT NULL DEFAULT 0,
    active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_shop
        FOREIGN KEY (shop_id)
        REFERENCES shops (id),

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories (id)
        ON DELETE SET NULL,

    -- Business requirement: inventory must never be negative
    CONSTRAINT chk_products_quantity
        CHECK (quantity >= 0)
);

CREATE INDEX idx_products_shop_id ON products (shop_id);
CREATE INDEX idx_products_category_id ON products (category_id);
