-- FR-BOT-007: Shop Owner marks bottles that will never be returned as LOST
-- or DAMAGED, closing them out of the "outstanding" count reported by
-- FR-BOT-006/008. No ledger entry is posted — the customer's deposit was
-- already debited at order placement and is simply forfeited (kept by the
-- shop) rather than refunded, which is the existing no-entry default.
CREATE TABLE bottle_adjustments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id      BIGINT       NOT NULL,
    customer_id    BIGINT       NOT NULL,
    quantity       INT          NOT NULL,
    type           VARCHAR(20)  NOT NULL,
    reason         VARCHAR(500) NOT NULL,
    created_by_user_id BIGINT   NOT NULL,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bottle_adj_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id),
    CONSTRAINT fk_bottle_adj_customer FOREIGN KEY (customer_id) REFERENCES users (id)
);
