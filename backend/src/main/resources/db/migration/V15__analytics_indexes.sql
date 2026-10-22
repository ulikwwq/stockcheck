CREATE INDEX IF NOT EXISTS idx_sales_shop_created
    ON sales(shop_id, created_at);

CREATE INDEX IF NOT EXISTS idx_expenses_tenant_created
    ON expenses(tenant_id, created_at);
