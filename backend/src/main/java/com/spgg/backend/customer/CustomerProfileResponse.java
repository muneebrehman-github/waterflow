package com.spgg.backend.customer;

import com.spgg.backend.user.UserStatus;

public record CustomerProfileResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        UserStatus status,
        Long shopId,
        String shopName) {
}
