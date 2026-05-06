package com.elif.aiservice.mcp;

public record JsonRpcError(
        int code,
        String message,
        Object data
) {}
