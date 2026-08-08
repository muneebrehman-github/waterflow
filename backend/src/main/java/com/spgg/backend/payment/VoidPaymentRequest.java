package com.spgg.backend.payment;

import jakarta.validation.constraints.NotBlank;

public record VoidPaymentRequest(@NotBlank String reason) {
}
