package com.elif.mcpproject.chat.dto;

import java.time.Instant;

public record MessageResponse (
        Long id,
        String role,
        String status,
        String content,
        Instant createdAt
){
}
