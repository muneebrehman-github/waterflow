package com.spgg.backend.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Shared body for actions that require a mandatory reason (reject/suspend). */
public record ReasonRequest(@NotBlank @Size(max = 500) String reason) {
}
