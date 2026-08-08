-- Tiered delivery pricing (FR-SHP-013): a JSON array of
-- {minOrderValue, maxOrderValue (nullable = "and above"), charge} rules.
-- A flat fee is just a single tier; free-above-threshold is two tiers.
ALTER TABLE shops
    ADD COLUMN delivery_charge_tiers_json TEXT NULL AFTER min_order_value;
