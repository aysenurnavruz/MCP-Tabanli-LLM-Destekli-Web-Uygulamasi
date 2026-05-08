package com.elif.mcpproject.document.dto;

public record DocumentReprocessResponse(
        Long documentId,
        int chunkCount,
        boolean indexed
) {
}
