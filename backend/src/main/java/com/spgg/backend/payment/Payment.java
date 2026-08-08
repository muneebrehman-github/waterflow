package com.spgg.backend.payment;

import com.spgg.backend.common.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FR-PAY-001/002: a manual payment (cash, bank transfer, etc.) recorded by
 * the Shop Owner/Staff — a general credit against the customer's ledger
 * balance, not tied to a specific order. BR-008/FR-PAY-005: immutable —
 * voiding sets {@code status} and {@code voidReason} but never deletes the
 * row; the reversing ledger entry is a separate {@code LedgerEntry}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends TenantAwareEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Column(name = "reference")
    private String reference;

    @Column(name = "receipt_number", nullable = false)
    private String receiptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.ACTIVE;

    @Column(name = "void_reason")
    private String voidReason;

    @Column(name = "recorded_by_user_id", nullable = false)
    private Long recordedByUserId;
}
