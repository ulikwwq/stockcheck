ALTER TABLE sales
    ADD COLUMN returned BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_sales_returned ON sales (returned);