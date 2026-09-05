-- Phase 3: purchase price and sale price become optional on products.
--
-- Consequently sale_items.purchase_price and sale_items.profit must also
-- allow NULL: a sale of a product with no recorded purchase price has a
-- known revenue but a genuinely unknown profit, which must be represented
-- explicitly rather than defaulted to 0.

ALTER TABLE products ALTER COLUMN purchase_price DROP NOT NULL;
ALTER TABLE products ALTER COLUMN default_sale_price DROP NOT NULL;

ALTER TABLE sale_items ALTER COLUMN purchase_price DROP NOT NULL;
ALTER TABLE sale_items ALTER COLUMN profit DROP NOT NULL;
