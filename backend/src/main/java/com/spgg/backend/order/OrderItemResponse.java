package com.spgg.backend.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId, String productName, int quantity, BigDecimal unitPrice,
        BigDecimal depositAmount, BigDecimal lineTotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(), item.getProductName(), item.getQuantity(),
                item.getUnitPrice(), item.getDepositAmount(), item.getLineTotal());
    }
}
