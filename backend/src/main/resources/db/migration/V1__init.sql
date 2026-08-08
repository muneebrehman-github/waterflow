-- Core identity + tenant (shop) tables for the first vertical slice:
-- Auth, Shop registration & Super Admin approval workflow.
--
-- Creation order matters: users and shops reference each other
-- (shops.owner_user_id -> users.id, users.tenant_id -> shops.id), so users
-- is created first without its tenant FK, then shops, then the FK is added
-- back onto users once shops exists.

CREATE TABLE users (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT        NULL,
    full_name           VARCHAR(150)  NOT NULL,
    email               VARCHAR(150)  NOT NULL,
    phone               VARCHAR(30),
    password_hash       VARCHAR(255)  NOT NULL,
    role                VARCHAR(20)   NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE shops (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id       BIGINT        NOT NULL,
    name                VARCHAR(150)  NOT NULL,
    description         VARCHAR(1000),
    owner_name          VARCHAR(150)  NOT NULL,
    email               VARCHAR(150)  NOT NULL,
    phone               VARCHAR(30)   NOT NULL,
    address             VARCHAR(500),
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING_APPROVAL',
    rejection_reason    VARCHAR(500),
    suspension_reason   VARCHAR(500),
    business_hours_json TEXT,
    delivery_hours_json TEXT,
    delivery_radius_km  DECIMAL(6,2),
    min_order_value     DECIMAL(10,2),
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_shops_email UNIQUE (email),
    CONSTRAINT fk_shops_owner_user FOREIGN KEY (owner_user_id) REFERENCES users (id)
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES shops (id);
