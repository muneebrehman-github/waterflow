package com.spgg.backend.shop;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record DeliverySettingsRequest(
        @NotNull @PositiveOrZero BigDecimal deliveryRadiusKm,
        @NotNull @PositiveOrZero BigDecimal minOrderValue,
        @NotNull List<DeliveryChargeTier> deliveryChargeTiers) {
}
