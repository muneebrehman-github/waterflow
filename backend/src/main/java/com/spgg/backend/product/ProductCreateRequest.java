package com.spgg.backend.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull @PositiveOrZero BigDecimal unitPrice,
        @NotNull @PositiveOrZero BigDecimal depositAmount,
        Integer displayOrder) {
}
