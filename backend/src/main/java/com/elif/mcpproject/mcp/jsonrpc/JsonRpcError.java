package com.elif.mcpproject.mcp.jsonrpc;

public record JsonRpcError(int code, String message, Object data){}