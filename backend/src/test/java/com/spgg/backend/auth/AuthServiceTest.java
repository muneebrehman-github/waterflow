package com.spgg.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.spgg.backend.common.ForbiddenException;
import com.spgg.backend.security.JwtTokenProvider;
import com.spgg.backend.security.RefreshTokenStore;
import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import com.spgg.backend.user.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenStore refreshTokenStore;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenStore);
    }

    private User activeUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("owner@example.com");
        user.setPasswordHash("hashed");
        user.setFullName("Shop Owner");
        user.setRole(Role.SHOP_OWNER);
        user.setStatus(UserStatus.ACTIVE);
        user.setTenantId(42L);
        return user;
    }

    @Test
    void login_issuesTokens_whenCredentialsAreValidAndAccountIsActive() {
        User user = activeUser();
        when(userRepository.findByEmailAndDeletedFalse("owner@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(user))
                .thenReturn(new JwtTokenProvider.GeneratedRefreshToken("refresh-token", "jti-123"));

        AuthResponse response = authService.login(new LoginRequest("owner@example.com", "correct-password"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tenantId()).isEqualTo(42L);
    }

    @Test
    void login_throwsBadCredentials_whenPasswordDoesNotMatch() {
        User user = activeUser();
        when(userRepository.findByEmailAndDeletedFalse("owner@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("owner@example.com", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_throwsBadCredentials_whenEmailIsUnknown_avoidingUserEnumeration() {
        when(userRepository.findByEmailAndDeletedFalse(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "whatever")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_throwsForbidden_whenShopOwnerIsStillPendingApproval() {
        User user = activeUser();
        user.setStatus(UserStatus.PENDING);
        when(userRepository.findByEmailAndDeletedFalse("owner@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("owner@example.com", "correct-password")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("pending");
    }

    @Test
    void login_throwsForbidden_whenAccountIsBlocked() {
        User user = activeUser();
        user.setStatus(UserStatus.BLOCKED);
        when(userRepository.findByEmailAndDeletedFalse("owner@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("owner@example.com", "correct-password")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("blocked");
    }
}
