-- FR-NOT-007: FCM device tokens, refreshed on each app launch. Not tenant-
-- scoped (mirrors users — a device token belongs to a user account, not a
-- shop). One row per physical token; re-registering under a different user
-- (e.g. logout/login as someone else on the same device) reassigns it.
CREATE TABLE device_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL,
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_device_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_device_tokens_token UNIQUE (token)
);
