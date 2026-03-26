package com.elif.mcpproject.chat.dto;

import java.time.Instant;

public record ChatResponse(
        Long id,
        Long documentId,
        String title,
        Instant createdAt,
        Instant updatedAt
){}