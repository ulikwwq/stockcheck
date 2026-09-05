-- A product may be created without a configured sale price (see
-- V8__optional_product_pricing.sql). Previously, selling such a product
-- still required the seller to manually type in a price for that sale.
-- That is no longer required: sale_items.sale_price must allow NULL so a
-- sale with a genuinely unknown price is recorded as such, rather than
-- forcing a price to be invented.

ALTER TABLE sale_items ALTER COLUMN sale_price DROP NOT NULL;
