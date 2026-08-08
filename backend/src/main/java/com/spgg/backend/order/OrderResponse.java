package com.spgg.backend.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        String customerName,
        String customerPhone,
        Long addressId,
        String addressLabel,
        String addressText,
        String addressArea,
        Long deliveryBoyId,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal deliveryCharge,
        BigDecimal discountAmount,
        BigDecimal orderAmount,
        BigDecimal totalDeposit,
        Integer totalBottleQuantity,
        Integer emptyBottlesCollected,
        BigDecimal totalPayable,
        String deliveryNotes,
        String statusReason,
        List<OrderItemResponse> items,
        Instant createdAt) {

    public static OrderResponse from(Order order, List<OrderItemResponse> items, String customerName,
            String customerPhone, String addressLabel, String addressText, String addressArea) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                customerName,
                customerPhone,
                order.getAddressId(),
                addressLabel,
                addressText,
                addressArea,
                order.getDeliveryBoyId(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDeliveryCharge(),
                order.getDiscountAmount(),
                order.getOrderAmount(),
                order.getTotalDeposit(),
                order.getTotalBottleQuantity(),
                order.getEmptyBottlesCollected(),
                order.getTotalPayable(),
                order.getDeliveryNotes(),
                order.getStatusReason(),
                items,
                order.getCreatedAt());
    }
}
