package com.elif.mcpproject.chat;

import com.elif.mcpproject.document.Document;
import com.elif.mcpproject.user.AppUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "chats")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private AppUser user;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(length = 120)
    private String title;

    @Column(nullable = false,updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    void onCreate(){
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = Instant.now();
    }
}
