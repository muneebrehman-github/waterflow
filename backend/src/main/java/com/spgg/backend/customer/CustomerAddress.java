package com.spgg.backend.customer;

import com.spgg.backend.common.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_addresses")
public class CustomerAddress extends TenantAwareEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "address_text", nullable = false)
    private String addressText;

    @Column(name = "area")
    private String area;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
