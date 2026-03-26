package com.elif.mcpproject.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final byte[] keyBytes;
    private final long accessMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-token-minutes}") long accessMinutes){
        this.keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.accessMinutes = accessMinutes;
    }

    public String generateAccessToken(String email){
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessMinutes*60);

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
