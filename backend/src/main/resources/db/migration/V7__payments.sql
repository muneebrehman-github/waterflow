-- FR-PAY-001/002: Shop Owner/Staff record a manual payment (cash, bank
-- transfer, etc.) as a general credit against the customer's ledger balance
-- (not linked to a specific order). FR-PAY-005/BR-009: void requires a
-- mandatory reason and must post an equal offsetting debit, never edit or
-- delete the original row (BR-008).
CREATE TABLE payments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT        NOT NULL,
    customer_id         BIGINT        NOT NULL,
    amount              DECIMAL(10,2) NOT NULL,
    method              VARCHAR(20)   NOT NULL,
    reference           VARCHAR(255),
    receipt_number      VARCHAR(50)   NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    void_reason         VARCHAR(500),
    recorded_by_user_id BIGINT        NOT NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id),
    CONSTRAINT fk_payments_customer FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT uq_payments_receipt_number UNIQUE (receipt_number)
);
