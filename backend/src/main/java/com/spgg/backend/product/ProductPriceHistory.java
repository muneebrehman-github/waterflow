package com.spgg.backend.product;

import com.spgg.backend.common.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_price_history")
public class ProductPriceHistory extends TenantAwareEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;
}
