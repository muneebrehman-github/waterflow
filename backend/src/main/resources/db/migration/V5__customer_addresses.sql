-- Customer identity/credentials reuse the shared `users` table (role=CUSTOMER,
-- tenant_id = the one shop they're scoped to per BR-001). Only delivery
-- addresses are genuinely new data (FR-CST-004/005).
CREATE TABLE customer_addresses (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    label        VARCHAR(50)  NOT NULL,
    address_text VARCHAR(500) NOT NULL,
    area         VARCHAR(150),
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_addresses_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id)
);
