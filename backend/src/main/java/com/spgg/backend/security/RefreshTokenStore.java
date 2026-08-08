package com.spgg.backend.security;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Tracks live refresh tokens in Redis (key = jti, value = userId), so a
 * refresh token can be validated and individually revoked on logout without
 * needing a DB round trip. TTL mirrors the token's own expiry.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void store(String jti, Long userId) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + jti,
                String.valueOf(userId),
                Duration.ofDays(jwtProperties.getRefreshTokenTtlDays()));
    }

    public boolean isValid(String jti, Long userId) {
        String storedUserId = redisTemplate.opsForValue().get(KEY_PREFIX + jti);
        return storedUserId != null && storedUserId.equals(String.valueOf(userId));
    }

    public void revoke(String jti) {
        redisTemplate.delete(KEY_PREFIX + jti);
    }
}
