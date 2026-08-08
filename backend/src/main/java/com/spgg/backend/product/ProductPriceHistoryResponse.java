package com.spgg.backend.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductPriceHistoryResponse(BigDecimal price, Instant changedAt) {

    public static ProductPriceHistoryResponse from(ProductPriceHistory history) {
        return new ProductPriceHistoryResponse(history.getPrice(), history.getCreatedAt());
    }
}
