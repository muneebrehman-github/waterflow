-- Refresh tokens are persisted in MariaDB (same durable volume as users) so
-- sessions survive backend container restarts. Redis was ephemeral for this
-- use-case and caused forced re-logins after docker restarts.
CREATE TABLE refresh_tokens (
    jti        VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    expires_at DATETIME     NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
