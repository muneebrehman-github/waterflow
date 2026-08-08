-- FR-DEL-001/003/004: Shop Owner/Staff assign a PREPARING order to a
-- Delivery Boy (order moves to OUT_FOR_DELIVERY); the Delivery Boy then
-- confirms delivery, optionally recording empty bottles collected
-- (FR-BOT-003/004), which triggers a proportional DEPOSIT_CREDIT refund.
ALTER TABLE orders
    ADD COLUMN delivery_boy_id       BIGINT NULL AFTER address_id,
    ADD COLUMN total_bottle_quantity INT    NOT NULL DEFAULT 0 AFTER total_deposit,
    ADD COLUMN empty_bottles_collected INT  NULL AFTER total_bottle_quantity,
    ADD CONSTRAINT fk_orders_delivery_boy FOREIGN KEY (delivery_boy_id) REFERENCES users (id);
