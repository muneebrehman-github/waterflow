package com.spgg.backend.bottle;

import java.math.BigDecimal;

public record ShopBottleSummaryResponse(
        int totalDelivered,
        int totalReturned,
        int totalLost,
        int totalDamaged,
        int totalOutstanding,
        BigDecimal estimatedOutstandingValue) {
}
