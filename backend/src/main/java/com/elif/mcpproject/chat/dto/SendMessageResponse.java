package com.elif.mcpproject.chat.dto;

public record SendMessageResponse(
        MessageResponse userMessage,
        MessageResponse assistantMessage
) {
}
