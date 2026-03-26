package com.elif.mcpproject.document;

import com.elif.mcpproject.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Entity
@Table(name="documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "storage_path", nullable = false,length = 500)
    private String storagePath;

    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(name = "created_at", nullable = false,updatable = false)
    private Instant createdAt;

}
