package com.spgg.backend.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull PaymentMethod method,
        String reference) {
}
