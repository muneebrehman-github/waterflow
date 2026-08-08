package com.spgg.backend.bottle;

import com.spgg.backend.common.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** FR-BOT-007: closes bottles out of the outstanding count when they'll never be returned. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bottle_adjustments")
public class BottleAdjustment extends TenantAwareEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BottleAdjustmentType type;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;
}
