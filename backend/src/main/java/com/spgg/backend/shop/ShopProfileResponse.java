package com.spgg.backend.shop;

import java.math.BigDecimal;
import java.util.List;

public record ShopProfileResponse(
        Long id,
        String name,
        String description,
        String ownerName,
        String email,
        String phone,
        String address,
        ShopStatus status,
        List<DayHours> businessHours,
        List<DayHours> deliveryHours,
        BigDecimal deliveryRadiusKm,
        BigDecimal minOrderValue,
        List<DeliveryChargeTier> deliveryChargeTiers) {
}
