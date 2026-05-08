package com.elif.mcpproject.chat.dto;

import java.util.List;

public record SendMessageResponse(
        MessageResponse userMessage,
        MessageResponse assistantMessage,
        List<CitationResponse> citations
) {
}
