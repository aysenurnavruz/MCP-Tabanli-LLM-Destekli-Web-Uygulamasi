CREATE TABLE document_chunks (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 document_id BIGINT NOT NULL,
                                 chunk_index INT NOT NULL,
                                 content LONGTEXT NOT NULL,
                                 start_offset INT NULL,
                                 end_offset INT NULL,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_document_chunks_document
                                     FOREIGN KEY (document_id)
                                         REFERENCES documents(id)
                                         ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_document_chunks_doc_idx
    ON document_chunks(document_id, chunk_index);

CREATE INDEX idx_document_chunks_doc
    ON document_chunks(document_id);

CREATE INDEX idx_document_chunks_offsets
    ON document_chunks(document_id, start_offset, end_offset);