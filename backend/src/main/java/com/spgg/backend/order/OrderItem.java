package com.spgg.backend.order;

import com.spgg.backend.common.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** productName/unitPrice/depositAmount are snapshotted at order time (BR-013). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem extends TenantAwareEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "deposit_amount", nullable = false)
    private BigDecimal depositAmount;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;
}
