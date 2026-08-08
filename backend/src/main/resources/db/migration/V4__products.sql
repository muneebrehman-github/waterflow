CREATE TABLE products (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id      BIGINT        NOT NULL,
    name           VARCHAR(150)  NOT NULL,
    unit_price     DECIMAL(10,2) NOT NULL,
    deposit_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    status         VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE',
    display_order  INT           NOT NULL DEFAULT 0,
    deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id)
);

-- FR-PRD-003: price change history retained for reporting accuracy.
-- One row per price ever set, including the initial price at creation.
-- created_at is the "changed at" timestamp (TenantAwareEntity's audit column).
CREATE TABLE product_price_history (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT        NOT NULL,
    product_id BIGINT        NOT NULL,
    price      DECIMAL(10,2) NOT NULL,
    deleted    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_history_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_price_history_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id)
);
