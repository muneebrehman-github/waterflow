package com.spgg.backend.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 50) String label,
        @NotBlank @Size(max = 500) String addressText,
        @Size(max = 150) String area,
        boolean isDefault) {
}
