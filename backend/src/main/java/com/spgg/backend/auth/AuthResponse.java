package com.spgg.backend.auth;

import com.spgg.backend.user.Role;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String fullName,
        Role role,
        Long tenantId) {
}
