package com.spgg.backend.notification;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceTokenRequest(@NotBlank String token) {
}
