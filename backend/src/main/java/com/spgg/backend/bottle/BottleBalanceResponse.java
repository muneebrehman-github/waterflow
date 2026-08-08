package com.spgg.backend.bottle;

public record BottleBalanceResponse(
        Long customerId,
        int delivered,
        int returned,
        int lost,
        int damaged,
        int outstanding) {
}
