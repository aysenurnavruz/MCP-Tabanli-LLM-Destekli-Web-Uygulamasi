package com.elif.mcpproject.document.dto;

import java.time.Instant;

public record DocumentResponse (
        Long id,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        Instant createdAt
){}

