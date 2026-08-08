package com.spgg.backend.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ProductPriceUpdateRequest(@NotNull @Positive BigDecimal unitPrice) {
}
