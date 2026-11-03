package com.spgg.backend.auth;

import com.spgg.backend.common.ForbiddenException;
import com.spgg.backend.common.UnauthorizedException;
import com.spgg.backend.security.JwtTokenProvider;
import com.spgg.backend.security.RefreshTokenStore;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import com.spgg.backend.user.UserStatus;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        assertActive(user);

        return issueTokens(user);
    }

    public AccessTokenResponse refresh(RefreshRequest request) {
        Claims claims = jwtTokenProvider.parseClaims(request.refreshToken());
        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new UnauthorizedException("Not a refresh token");
        }

        Long userId = jwtTokenProvider.getUserId(claims);
        String jti = claims.getId();
        if (!refreshTokenStore.isValid(jti, userId)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ForbiddenException("Account no longer exists"));
        assertActive(user);

        return new AccessTokenResponse(jwtTokenProvider.generateAccessToken(user));
    }

    public void logout(RefreshRequest request) {
        try {
            Claims claims = jwtTokenProvider.parseClaims(request.refreshToken());
            refreshTokenStore.revoke(claims.getId());
        } catch (RuntimeException ignored) {
            // Already invalid/expired — logout is idempotent either way.
        }
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        var refresh = jwtTokenProvider.generateRefreshToken(user);
        refreshTokenStore.store(refresh.jti(), user.getId());

        return new AuthResponse(
                accessToken,
                refresh.token(),
                user.getId(),
                user.getFullName(),
                user.getRole(),
                user.getTenantId());
    }

    private void assertActive(User user) {
        if (user.getStatus() == UserStatus.PENDING) {
            throw new ForbiddenException("Your shop registration is still pending Super Admin approval");
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException("This account has been blocked");
        }
    }
}
