-- Sample products for any ACTIVE shop that has none yet (local dev convenience).

INSERT INTO products (tenant_id, name, unit_price, deposit_amount, status, display_order)
SELECT s.id, '19L Water Bottle', 300.00, 500.00, 'AVAILABLE', 1
FROM shops s
WHERE s.status = 'ACTIVE'
  AND s.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.tenant_id = s.id AND p.deleted = FALSE);

INSERT INTO products (tenant_id, name, unit_price, deposit_amount, status, display_order)
SELECT s.id, '6L Water Bottle', 120.00, 200.00, 'AVAILABLE', 2
FROM shops s
WHERE s.status = 'ACTIVE'
  AND s.deleted = FALSE
  AND (SELECT COUNT(*) FROM products p WHERE p.tenant_id = s.id AND p.deleted = FALSE) = 1;

INSERT INTO product_price_history (tenant_id, product_id, price)
SELECT p.tenant_id, p.id, p.unit_price
FROM products p
WHERE p.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM product_price_history h WHERE h.product_id = p.id AND h.deleted = FALSE
  );
