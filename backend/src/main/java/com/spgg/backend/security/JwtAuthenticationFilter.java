package com.spgg.backend.security;

import com.spgg.backend.common.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the JWT access token on every request, populates the Spring
 * Security context, and stashes the caller's tenant id in {@link TenantContext}
 * for {@link TenantFilter} to pick up. Trusts the token's claims directly
 * (no DB round trip) — role/tenantId are only ever set at token-issuance
 * time in AuthService, so they're as trustworthy as the signature itself.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null) {
                Claims claims = jwtTokenProvider.parseClaims(token);
                if (jwtTokenProvider.isAccessToken(claims)) {
                    authenticate(claims);
                }
            }
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private void authenticate(Claims claims) {
        Long userId = jwtTokenProvider.getUserId(claims);
        var role = jwtTokenProvider.getRole(claims);
        Long tenantId = jwtTokenProvider.getTenantId(claims);

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        TenantContext.setTenantId(tenantId);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
