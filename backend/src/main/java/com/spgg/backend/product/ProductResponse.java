package com.spgg.backend.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal unitPrice,
        BigDecimal depositAmount,
        ProductStatus status,
        Integer displayOrder,
        Instant createdAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getDepositAmount(),
                product.getStatus(),
                product.getDisplayOrder(),
                product.getCreatedAt());
    }
}
