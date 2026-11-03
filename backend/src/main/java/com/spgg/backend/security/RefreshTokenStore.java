package com.spgg.backend.security;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Tracks live refresh tokens in MariaDB so sessions survive backend restarts.
 * Each row mirrors the refresh JWT's jti and expiry; revoked on logout.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public void store(String jti, Long userId) {
        RefreshToken token = new RefreshToken();
        token.setJti(jti);
        token.setUserId(userId);
        token.setExpiresAt(Instant.now().plus(Duration.ofDays(jwtProperties.getRefreshTokenTtlDays())));
        refreshTokenRepository.save(token);
    }

    public boolean isValid(String jti, Long userId) {
        return refreshTokenRepository.findByJtiAndRevokedFalse(jti)
                .filter(token -> token.getUserId().equals(userId))
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    public void revoke(String jti) {
        refreshTokenRepository.findById(jti).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}
