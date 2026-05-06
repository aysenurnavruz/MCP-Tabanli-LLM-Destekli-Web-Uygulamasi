package com.elif.mcpproject.auth.dto;

public record AuthResponse (
    String accessToken,
    String refreshToken
){}
