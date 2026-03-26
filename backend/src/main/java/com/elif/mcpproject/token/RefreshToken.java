package com.elif.mcpproject.token;

import com.elif.mcpproject.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Builder
@Table(name = "refresh_tokens")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private AppUser user;

    @Column(nullable = false,unique = true,length = 255)
    private String token;

    @Column(name = "expires_at",nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;
}
