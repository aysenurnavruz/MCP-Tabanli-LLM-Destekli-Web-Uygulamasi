package com.elif.mcpproject.auth;

import com.elif.mcpproject.auth.dto.AuthResponse;
import com.elif.mcpproject.auth.dto.ChangePasswordRequest;
import com.elif.mcpproject.auth.dto.LoginRequest;
import com.elif.mcpproject.auth.dto.RegisterRequest;
import com.elif.mcpproject.security.JwtService;
import com.elif.mcpproject.token.RefreshToken;
import com.elif.mcpproject.token.RefreshTokenRepository;
import com.elif.mcpproject.user.AppUser;
import com.elif.mcpproject.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-days}")
    private long refreshDays;

    public AuthResponse register(RegisterRequest req){
        if (userRepository.existsByEmail(req.email())){
            throw new ResponseStatusException(CONFLICT,"Email already exists");
        }

        Instant now = Instant.now();
        AppUser user = AppUser.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(AppUser.Role.USER)
                .createdAt(now)
                .build();

        userRepository.save(user);
        return issueTokens(user, now);
    }

    public AuthResponse login(LoginRequest req){
        AppUser user = userRepository.findByEmail(req.email())
                .orElseThrow(()-> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())){
            throw new ResponseStatusException(UNAUTHORIZED,"Invalid credentials");
        }
        revokeAllUserTokens(user);

        return issueTokens(user, Instant.now());
    }

    public AuthResponse issueTokens(AppUser user, Instant now){
        String access = jwtService.generateAccessToken(user.getEmail());
        String refresh = UUID.randomUUID().toString();

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .token(refresh)
                        .expiresAt(now.plusSeconds(refreshDays*24*60*60))
                        .revokedAt(null)
                        .createdAt(now)
                        .build()
        );

        return new AuthResponse(access,refresh);
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Token not found"));

        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
    }

    public AuthResponse refresh(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token"));

        if (token.getRevokedAt() != null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token revoked");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token expired");
        }
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);

        AppUser user = token.getUser();
        return issueTokens(user, Instant.now());
    }

    public void changePassword(ChangePasswordRequest req, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
        }

        AppUser user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "Current password is incorrect");
        }

        if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        revokeAllUserTokens(user);
    }

    private void revokeAllUserTokens(AppUser user) {
        var tokens = refreshTokenRepository.findByUser(user);
        Instant now = Instant.now();
        tokens.forEach(t -> t.setRevokedAt(now));
        refreshTokenRepository.saveAll(tokens);
    }

}
