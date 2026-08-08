package com.spgg.backend.security;

import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TENANT_ID = "tenantId";
    private static final String CLAIM_FULL_NAME = "fullName";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    private final JwtProperties jwtProperties;

    private javax.crypto.SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TENANT_ID, user.getTenantId())
                .claim(CLAIM_FULL_NAME, user.getFullName())
                .claim(CLAIM_TOKEN_TYPE, TokenType.ACCESS.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(jwtProperties.getAccessTokenTtlMinutes()))))
                .signWith(signingKey())
                .compact();
    }

    /** Returns the raw token plus its jti, so the caller can index the jti in Redis. */
    public GeneratedRefreshToken generateRefreshToken(User user) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_TOKEN_TYPE, TokenType.REFRESH.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofDays(jwtProperties.getRefreshTokenTtlDays()))))
                .signWith(signingKey())
                .compact();
        return new GeneratedRefreshToken(token, jti);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public Role getRole(Claims claims) {
        String role = claims.get(CLAIM_ROLE, String.class);
        return role == null ? null : Role.valueOf(role);
    }

    public Long getTenantId(Claims claims) {
        Object tenantId = claims.get(CLAIM_TENANT_ID);
        return tenantId == null ? null : Long.valueOf(tenantId.toString());
    }

    public String getFullName(Claims claims) {
        return claims.get(CLAIM_FULL_NAME, String.class);
    }

    public boolean isAccessToken(Claims claims) {
        return TokenType.ACCESS.name().equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return TokenType.REFRESH.name().equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    public record GeneratedRefreshToken(String token, String jti) {
    }
}
