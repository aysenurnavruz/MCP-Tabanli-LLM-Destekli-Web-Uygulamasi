package com.elif.mcpproject.document.text;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_chunks",
uniqueConstraints = @UniqueConstraint(name="uq_document_chunks_doc_idx", columnNames = {"document_id","chunk_index"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id",nullable = false)
    private Long documentId;

    @Column(name = "chunk_index",nullable = false)
    private Integer chunkIndex;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "start_offset")
    private Integer startOffset;

    @Column(name = "end_offset")
    private Integer endOffset;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}
