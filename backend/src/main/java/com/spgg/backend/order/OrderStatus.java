package com.spgg.backend.order;

/** BR-005: strictly sequential PENDING -> ACCEPTED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED,
 *  no backward transitions except the explicit REJECTED/CANCELLED exits.
 *  OUT_FOR_DELIVERY/DELIVERED are reached via the Delivery Assignment slice, not here. */
public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    REJECTED,
    CANCELLED
}
