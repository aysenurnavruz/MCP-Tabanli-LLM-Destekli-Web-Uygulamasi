package com.elif.mcpproject.chat;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;


    @Column(name = "client_message_id", length = 36,unique = true)
    private String clientMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Status status;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String content;

    @Column(nullable = false,updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = Status.CREATED;
    }

    public enum Role{
        USER,
        ASSISTANT
    }

    public enum Status{
        CREATED,
        STREAMING,
        COMPLETED,
        FAILED
    }
}
