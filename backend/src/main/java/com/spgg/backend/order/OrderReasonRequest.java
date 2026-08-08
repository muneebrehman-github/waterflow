package com.spgg.backend.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderReasonRequest(@NotBlank @Size(max = 500) String reason) {
}
