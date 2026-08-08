package com.spgg.backend.staff;

import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserStatus;

public record StaffSummaryResponse(Long id, String fullName, String phone, String email, Role role, UserStatus status) {

    public static StaffSummaryResponse from(User user) {
        return new StaffSummaryResponse(user.getId(), user.getFullName(), user.getPhone(), user.getEmail(), user.getRole(), user.getStatus());
    }
}
