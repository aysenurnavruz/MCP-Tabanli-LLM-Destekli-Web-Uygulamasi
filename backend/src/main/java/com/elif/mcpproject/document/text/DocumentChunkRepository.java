package com.elif.mcpproject.document.text;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findAllByDocumentIdOrderByChunkIndexAsc(Long documentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DocumentChunk chunk where chunk.document.id = :documentId")
    void deleteAllByDocumentId(@Param("documentId") Long documentId);
}
