package com.spgg.backend.shop;

import java.math.BigDecimal;

/** {@code maxOrderValue} null means "and above" — the last/highest tier. */
public record DeliveryChargeTier(BigDecimal minOrderValue, BigDecimal maxOrderValue, BigDecimal charge) {
}
