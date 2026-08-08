-- BR-007: every financial transaction must produce a ledger entry, so a
-- minimal ledger foundation is introduced here (Orders is the first thing
-- that writes to it) — the dedicated Customer Ledger slice mainly exposes
-- this data (statements, running balance) rather than building it from
-- scratch.

CREATE TABLE orders (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id         BIGINT        NOT NULL,
    customer_id       BIGINT        NOT NULL,
    address_id        BIGINT        NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    subtotal          DECIMAL(10,2) NOT NULL,
    delivery_charge   DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount   DECIMAL(10,2) NOT NULL DEFAULT 0,
    order_amount      DECIMAL(10,2) NOT NULL,
    total_deposit     DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_payable     DECIMAL(10,2) NOT NULL,
    delivery_notes    VARCHAR(500),
    status_reason     VARCHAR(500),
    deleted           BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_orders_address FOREIGN KEY (address_id) REFERENCES customer_addresses (id)
);

-- Product name/price/deposit are snapshotted here at order time (BR-013:
-- price changes must not affect historical orders).
CREATE TABLE order_items (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id      BIGINT        NOT NULL,
    order_id       BIGINT        NOT NULL,
    product_id     BIGINT        NOT NULL,
    product_name   VARCHAR(150)  NOT NULL,
    quantity       INT           NOT NULL,
    unit_price     DECIMAL(10,2) NOT NULL,
    deposit_amount DECIMAL(10,2) NOT NULL,
    line_total     DECIMAL(10,2) NOT NULL,
    deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- BR-008: ledger entries are immutable — corrections are new offsetting
-- entries, never edits. `amount` is a signed delta to the running balance
-- (positive = customer owes more, negative = customer owes less);
-- `type` is descriptive/categorical only (DEBIT, CREDIT, DEPOSIT_DEBIT,
-- DEPOSIT_CREDIT, ADJUSTMENT), not used to derive the sign.
CREATE TABLE ledger_entries (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id                BIGINT        NOT NULL,
    customer_id              BIGINT        NOT NULL,
    type                     VARCHAR(20)   NOT NULL,
    amount                   DECIMAL(10,2) NOT NULL,
    running_balance_snapshot DECIMAL(10,2) NOT NULL,
    reference_type           VARCHAR(30)   NOT NULL,
    reference_id             BIGINT        NOT NULL,
    description              VARCHAR(500),
    deleted                  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id),
    CONSTRAINT fk_ledger_customer FOREIGN KEY (customer_id) REFERENCES users (id)
);
