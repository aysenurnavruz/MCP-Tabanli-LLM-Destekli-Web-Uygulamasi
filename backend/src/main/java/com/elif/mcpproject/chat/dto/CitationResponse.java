package com.elif.mcpproject.chat.dto;

public record CitationResponse(
        Long chunkId,
        Integer chunkIndex,
        Integer pageStart,
        Integer pageEnd,
        Integer startOffset,
        Integer endOffset,
        double score,
        String preview
) {
}
