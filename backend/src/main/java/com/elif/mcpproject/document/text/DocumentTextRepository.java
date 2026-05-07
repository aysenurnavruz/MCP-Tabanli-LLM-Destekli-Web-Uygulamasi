package com.elif.mcpproject.document.text;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentTextRepository extends JpaRepository<DocumentText, Long> {
    Optional<DocumentText> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
}
