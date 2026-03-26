package com.elif.mcpproject.document.text;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findAllByDocumentIdOrderByChunkIndexAsc(Long documentId);
    void deleteAllByDocumentId(Long documentId);
}
