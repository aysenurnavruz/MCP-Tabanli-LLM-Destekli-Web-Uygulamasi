package com.elif.mcpproject.chat.dto;

public record MessageRequest (
        String content,
        String clientMessageId
){}
