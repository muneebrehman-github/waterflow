package com.spgg.backend.product;

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

/** First entity to actually extend {@link TenantAwareEntity} — tenant isolation
 *  via the Hibernate "tenantFilter" is exercised for real starting here. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends TenantAwareEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "deposit_amount", nullable = false)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}
