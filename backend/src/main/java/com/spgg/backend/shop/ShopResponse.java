package com.spgg.backend.shop;

import java.time.Instant;

public record ShopResponse(
        Long id,
        String name,
        String description,
        String ownerName,
        String email,
        String phone,
        String address,
        ShopStatus status,
        String rejectionReason,
        String suspensionReason,
        Instant createdAt) {

    public static ShopResponse from(Shop shop) {
        return new ShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getDescription(),
                shop.getOwnerName(),
                shop.getEmail(),
                shop.getPhone(),
                shop.getAddress(),
                shop.getStatus(),
                shop.getRejectionReason(),
                shop.getSuspensionReason(),
                shop.getCreatedAt());
    }
}
