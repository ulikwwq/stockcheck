-- Phase 2, Part 3: Sales, Sale Items, Stock Movements, and Audit Logs

CREATE TABLE sales (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id      UUID NOT NULL,
    seller_id    UUID NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sales_shop
        FOREIGN KEY (shop_id)
        REFERENCES shops (id),

    CONSTRAINT fk_sales_seller
        FOREIGN KEY (seller_id)
        REFERENCES users (id)
);

CREATE INDEX idx_sales_shop_id ON sales (shop_id);
CREATE INDEX idx_sales_seller_id ON sales (seller_id);
CREATE INDEX idx_sales_created_at ON sales (created_at);

CREATE TABLE sale_items (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id        UUID NOT NULL,
    product_id     UUID NOT NULL,
    quantity       INT NOT NULL CHECK (quantity > 0),
    purchase_price NUMERIC(12, 2) NOT NULL,
    sale_price     NUMERIC(12, 2) NOT NULL,
    profit         NUMERIC(12, 2) NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sale_items_sale
        FOREIGN KEY (sale_id)
        REFERENCES sales (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sale_items_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
);

CREATE INDEX idx_sale_items_sale_id ON sale_items (sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items (product_id);

CREATE TABLE stock_movements (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL,
    type            VARCHAR(32) NOT NULL,
    quantity_change INT NOT NULL,
    user_id         UUID,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (product_id)
        REFERENCES products (id),

    CONSTRAINT fk_stock_movements_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE INDEX idx_stock_movements_product_id ON stock_movements (product_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements (created_at);

CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL,
    user_id     UUID,
    action      VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id   UUID,
    details     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),

    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE INDEX idx_audit_logs_tenant_id ON audit_logs (tenant_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
