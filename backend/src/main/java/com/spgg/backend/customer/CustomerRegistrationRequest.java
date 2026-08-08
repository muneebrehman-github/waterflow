package com.spgg.backend.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * FR-CST-001 allows email to be optional for customers, but this system uses
 * email as the universal login identifier for every role (users.email is
 * NOT NULL + UNIQUE) — making it required here is a deliberate Phase-1
 * simplification rather than building out phone-based login as an alternative.
 */
public record CustomerRegistrationRequest(
        @NotNull Long shopId,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String password) {
}
