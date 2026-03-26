CREATE TABLE documents (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           original_filename VARCHAR(255) NOT NULL,
                           stored_filename VARCHAR(255) NOT NULL,
                           content_type VARCHAR(120) NULL,
                           size_bytes BIGINT NOT NULL,
                           storage_path VARCHAR(500) NOT NULL,
                           extracted_text LONGTEXT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_documents_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_documents_user_created ON documents(user_id, created_at);