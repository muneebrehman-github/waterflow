package com.spgg.backend.shop;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShopRegistrationRequest(
        @NotBlank @Size(max = 150) String shopName,
        @NotBlank @Size(max = 150) String ownerName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Size(min = 8, max = 100) String password,
        @Size(max = 1000) String description,
        @Size(max = 500) String address) {
}
