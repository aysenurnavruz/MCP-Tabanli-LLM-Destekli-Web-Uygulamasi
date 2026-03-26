CREATE TABLE refresh_tokens (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                token VARCHAR(255) NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                revoked_at TIMESTAMP NULL,
                                last_used_at TIMESTAMP NULL,
                                CONSTRAINT fk_refresh_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE,
                                CONSTRAINT uq_refresh_token UNIQUE (token)
);

CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_expires ON refresh_tokens(expires_at);

CREATE INDEX idx_refresh_user_revoked ON refresh_tokens(user_id, revoked_at);
CREATE INDEX idx_refresh_last_used ON refresh_tokens(last_used_at);