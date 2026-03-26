CREATE TABLE document_texts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    extracted_text LONGTEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_document_texts_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_document_texts_document
        UNIQUE (document_id)
);

CREATE INDEX idx_document_texts_status
    ON document_texts(status);