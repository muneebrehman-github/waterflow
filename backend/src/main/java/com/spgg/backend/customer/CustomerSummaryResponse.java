package com.spgg.backend.customer;

import com.spgg.backend.user.User;
import com.spgg.backend.user.UserStatus;

public record CustomerSummaryResponse(Long id, String fullName, String phone, String email, UserStatus status) {

    public static CustomerSummaryResponse from(User user) {
        return new CustomerSummaryResponse(user.getId(), user.getFullName(), user.getPhone(), user.getEmail(), user.getStatus());
    }
}
