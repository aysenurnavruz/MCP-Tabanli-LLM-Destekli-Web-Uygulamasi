ALTER TABLE document_chunks
    ADD COLUMN page_start INT NULL,
    ADD COLUMN page_end INT NULL;

CREATE INDEX idx_document_chunks_pages
    ON document_chunks(document_id, page_start, page_end);
