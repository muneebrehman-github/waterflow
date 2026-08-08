package com.spgg.backend.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long customerId,
        BigDecimal amount,
        PaymentMethod method,
        String reference,
        String receiptNumber,
        PaymentStatus status,
        String voidReason,
        Instant createdAt) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getReference(),
                payment.getReceiptNumber(),
                payment.getStatus(),
                payment.getVoidReason(),
                payment.getCreatedAt());
    }
}
