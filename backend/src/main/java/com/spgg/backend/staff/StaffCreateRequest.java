package com.spgg.backend.staff;

import com.spgg.backend.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StaffCreateRequest(
        @NotBlank String fullName,
        @NotBlank String phone,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull Role role) {
}
