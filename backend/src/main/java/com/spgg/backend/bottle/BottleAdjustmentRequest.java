package com.spgg.backend.bottle;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BottleAdjustmentRequest(
        @Min(1) int quantity,
        @NotNull BottleAdjustmentType type,
        @NotBlank String reason) {
}
