package com.spgg.backend.order;

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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends TenantAwareEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "address_id", nullable = false)
    private Long addressId;

    /** Set when Shop Owner/Staff assigns this order for delivery (FR-DEL-001). */
    @Column(name = "delivery_boy_id")
    private Long deliveryBoyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "delivery_charge", nullable = false)
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** subtotal + deliveryCharge - discountAmount — the amount ledgered as a plain DEBIT. */
    @Column(name = "order_amount", nullable = false)
    private BigDecimal orderAmount;

    /** Sum of line deposits — ledgered separately as a DEPOSIT_DEBIT (FR-BOT-002). */
    @Column(name = "total_deposit", nullable = false)
    private BigDecimal totalDeposit = BigDecimal.ZERO;

    /** Sum of item quantities carrying a deposit — the bottle count owed back (FR-BOT-005). */
    @Column(name = "total_bottle_quantity", nullable = false)
    private Integer totalBottleQuantity = 0;

    /** Set by the Delivery Boy on completion (FR-DEL-004/FR-BOT-003) — null until then. */
    @Column(name = "empty_bottles_collected")
    private Integer emptyBottlesCollected;

    /** orderAmount + totalDeposit — for display only, not itself ledgered as one entry. */
    @Column(name = "total_payable", nullable = false)
    private BigDecimal totalPayable;

    @Column(name = "delivery_notes")
    private String deliveryNotes;

    @Column(name = "status_reason")
    private String statusReason;
}
